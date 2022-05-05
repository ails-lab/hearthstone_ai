package net.demilich.metastone.game;

public enum Feature {
    HEALTH(0, 30, 0.044),
    HERO_ATTACK(1, 3, 0.102),
    CARDS(2, 10, 0.048),
    SECRETS(3, 3, 0.061),
    MINIONS(4, 7, 0.042),
    MINIONS_MANA(5, 60, 0.025),
    MINIONS_ATTACK(6, 60, 0.07),
    MINIONS_HP(7, 60, 0.03),
    TAUNT_MINIONS(8, 7, 0.031),
    OPP_HEALTH(9, 30, 0.047),
    OPP_HERO_ATTACK(10, 3, 0),
    OPP_CARDS(11, 10, 0.042),
    OPP_SECRETS(12, 3, 0.069),
    OPP_MINIONS(13, 7, 0.034),
    OPP_MINIONS_MANA(14, 60, 0.028),
    OPP_MINIONS_ATTACK(15, 60, 0.077),
    OPP_MINIONS_HP(16, 60, 0.034),
    OPP_TAUNT_MINIONS(17, 7, 0.035),
    MANA(18, 10, 0.032),
    //SPELLS(19, 10, 0.028),
    SPELLS(19, 10, 0),
    MINIONS_TO_ATTACK(20, 7, 0.031),
    MINIONS_AVAILABLE_ATTACK(21, 60, 0.061),
    TURN(22, 60, 0.027);


    private final int index;
    private final double max;
    private final double weight;


    Feature(int index, double max, double weight) {
        this.index = index;
        this.max = max;
        this.weight = weight;
    }

    public int getIndex() { return index; }
    public double getMax() { return max; }
    public double getWeight() { return weight; }

}
