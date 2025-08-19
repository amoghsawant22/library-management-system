package com.library.library_management_system.enums;

public enum Genre {
    FICTION("Fiction"),
    NON_FICTION("Non-Fiction"),
    MYSTERY("Mystery"),
    THRILLER("Thriller"),
    ROMANCE("Romance"),
    SCIENCE_FICTION("Science Fiction"),
    FANTASY("Fantasy"),
    BIOGRAPHY("Biography"),
    AUTOBIOGRAPHY("Autobiography"),
    HISTORY("History"),
    SCIENCE("Science"),
    TECHNOLOGY("Technology"),
    PHILOSOPHY("Philosophy"),
    PSYCHOLOGY("Psychology"),
    SELF_HELP("Self Help"),
    BUSINESS("Business"),
    ECONOMICS("Economics"),
    POLITICS("Politics"),
    RELIGION("Religion"),
    HEALTH("Health"),
    COOKING("Cooking"),
    TRAVEL("Travel"),
    ART("Art"),
    MUSIC("Music"),
    POETRY("Poetry"),
    DRAMA("Drama"),
    CHILDREN("Children"),
    YOUNG_ADULT("Young Adult"),
    EDUCATIONAL("Educational"),
    REFERENCE("Reference"),
    OTHER("Other");

    private final String displayName;

    Genre(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}