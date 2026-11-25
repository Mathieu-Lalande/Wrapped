package dev.wrapped.domain;

public enum PersonaType {
    HIBERNATING("Mode hibernation", "Une année au calme. Les meilleures idées mûrissent lentement."),
    MARATHONER("Mode marathon", "Des semaines entières sans lâcher le clavier. L'endurance, c'est ton truc."),
    TEAM_PLAYER("Esprit d'équipe", "Tu fais avancer le code des autres autant que le tien : les pull requests sont ta langue."),
    BUG_HUNTER("Chasse aux bugs", "Rien ne t'échappe : tu repères, tu signales, tu fais avancer les projets."),
    POLYGLOT("Polyglotte", "Un langage ? Trop peu. Tu passes de l'un à l'autre sans effort."),
    WEEKEND_WARRIOR("Week-end warrior", "Quand les autres se reposent, toi tu commits."),
    LASER_FOCUS("Laser focus", "Un projet, une obsession. Tu creuses profond plutôt que large."),
    STEADY_BUILDER("Bâtisseur régulier", "Pas de coup d'éclat, juste une constance qui finit par tout construire.");

    private final String title;
    private final String description;

    PersonaType(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }
}
