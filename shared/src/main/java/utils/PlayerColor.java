package utils;

public enum PlayerColor {
    WHITE,
    BLACK;

    @Override
    public String toString() {
        return name().toUpperCase();
    }
}