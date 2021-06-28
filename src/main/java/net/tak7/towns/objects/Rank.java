package net.tak7.towns.objects;

public enum Rank {
    ADMIN("admin", 0),
    OFFICIAL("official", 1),
    CIVILIAN("civilian", 2);

    private final String name;
    private final int order;

    Rank(String name, int order) {
        this.name = name;
        this.order = order;
    }

    public String getName() {
        return name;
    }

    public int getOrder() {
        return order;
    }

    public static Rank getNext(Rank current) {
        if (current == CIVILIAN) {
            return OFFICIAL;
        } else if (current == OFFICIAL) {
            return ADMIN;
        } else if (current == ADMIN) {
            return ADMIN;
        }
        return null;
    }

    public String getLetter() {
        return name.substring(0,1).toUpperCase();
    }
}