from wsgiref.simple_server import make_server
from tensorflow.keras.models import load_model
import json
import pandas as pd
import numpy as np
import os

path = os.path.dirname(os.path.abspath(__file__)) +'/'
safety_network_filename = path + "safety_network_uniform.h5"     #change this for bimodal-based safety net
safety_network = load_model(safety_network_filename)

probability_network_filename = path + 'probability_network_uniform.h5' #change this for bimodal-based probability net
probability_network = load_model(probability_network_filename)

def find_best_action_prob(actions, iterations, model):
    a = np.array([[actions, iterations]])
    return model.predict(a)[0][0]

def call(environ, start_response):
    status = '200 OK'
    headers = [
        ('Content-type', 'application/json'),
        ('Access-Control-Allow-Origin', '*'),
    ]
    start_response(status, headers)
    actions = pd.read_csv(path + 'actions.csv', header=None).to_numpy().flatten()[0]   #change this
    mcts_iterations = pd.read_csv(path + 'mctsIterations.csv', header=None).to_numpy().flatten()[0]
    tree_reuse_iterations = pd.read_csv(path + 'treeReuseIter.csv', header=None).to_numpy().flatten()[0]
    if 100+tree_reuse_iterations>1500:
        actions_to_keep = round(safety_network.predict(np.array([[actions, tree_reuse_iterations]]))[0][0])
        # sanity check
        if actions_to_keep > actions:
            actions_to_keep = actions
        prediction = {'best_iter': int(tree_reuse_iterations), 'actions_to_keep': int(actions_to_keep)}
        return [json.dumps(prediction).encode()]

    iterations = list(range(min(1500, 100+tree_reuse_iterations), min(mcts_iterations+tree_reuse_iterations, 1501)))
    if len(iterations) == 0:
        prediction = {'best_iter': int(tree_reuse_iterations), 'actions_to_keep': int(actions)}
        return [json.dumps(prediction).encode()]
    a = np.zeros((len(iterations), 2))

    a[:, 0] = actions
    a[:, 1] = iterations
    pred_discrete_actions = [round(x) for x in safety_network.predict(a).flatten()]
    pred_discrete_actions = [actions if x > actions else x for x in pred_discrete_actions]
    disc_actions, indices = np.unique(pred_discrete_actions, return_index=True)
    indices = np.flip(indices)
    disc_iter = np.ceil(a[indices, 1])
    if actions > disc_actions[-1]:
        l = list(disc_actions)
        if tree_reuse_iterations < 100:
            l.append(actions)
        else:
            l.append(round(safety_network.predict(np.array([[actions, tree_reuse_iterations]]))[0][0]))
        disc_actions = np.array(l)
        l = list(disc_iter)
        l.insert(0, tree_reuse_iterations)
        disc_iter = np.array(l)
    else:
        disc_iter[0] = tree_reuse_iterations

    disc_actions = np.flip(disc_actions)
    best_action_probs = []
    for i in range(len(disc_actions)):
        prob = find_best_action_prob(disc_actions[i], mcts_iterations+tree_reuse_iterations-disc_iter[i], probability_network)
        best_action_probs.append(prob)
    best_iter = disc_iter[np.argmax(best_action_probs)]
    actions_to_keep = disc_actions[np.argmax(best_action_probs)]
    # sanity check
    if actions_to_keep > actions:
        actions_to_keep = actions
    prediction = {'best_iter': int(best_iter), 'actions_to_keep': int(actions_to_keep)}
    return [json.dumps(prediction).encode()]

server = make_server('127.0.0.1', 8003, call)
server.serve_forever()

