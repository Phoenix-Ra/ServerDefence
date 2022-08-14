package FenixRa.ServerDefence;


public enum LangKeys {
    PREFIX("prefix"),
    CONSOLE_ONLY_CMD("console_only_cmd"),
    KICK_PERMISSION("kick_permission"),
    KICK_ALREADY_ONLINE("kick_already_online"),
    KICK_DIFFERENT_IP("kick_different_ip"),
    KICK_OP("kick_op");


    private final String value;

    LangKeys(final String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return Utils.colorFormat(Main.getInstance().fileM.getConfig("lang").getString(this.value));
    }

}
