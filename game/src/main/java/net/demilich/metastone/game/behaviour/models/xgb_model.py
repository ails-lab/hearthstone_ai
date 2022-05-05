from wsgiref.simple_server import make_server
import json
import pandas as pd
import pickle
import os

path = os.path.dirname(os.path.abspath(__file__)) +'/'
model_filename = path + "whs_mcts_best_xgb5.pkl"     #change this
with open(model_filename, 'rb') as file:
    model = pickle.load(file)

def call(environ, start_response):
    status = '200 OK'
    headers = [
        ('Content-type', 'application/json'),
        ('Access-Control-Allow-Origin', '*'),
    ]
    start_response(status, headers)
    state = pd.read_csv(path + 'state.csv', header=None)    #change this
    state = state.to_numpy().reshape(1,-1)
    #print('state: ', state)
    winner = model.predict(state)
    #print('prediction: ', winner)
    prediction = {'winner': int(winner[0])}
    return [json.dumps(prediction).encode()]

server = make_server('127.0.0.1', 8000, call)
server.serve_forever()

