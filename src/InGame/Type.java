package InGame;

import javafx.scene.paint.Color;

public enum Type {
    normal(Color.GRAY),
    fighting,
    flying,
    poison,
    ground,
    rock,
    bug,
    ghost,
    steel,
    fire,
    water,
    grass,
    electric,
    psychic,
    ice,
    dragon,
    dark,
    fairy;

    private Color c;

    Type(Color c) {
        this.c = c;
    }

    Type(){

    }

    public Color getColor() {
        return c;
    }
}
