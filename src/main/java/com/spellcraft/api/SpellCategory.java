package com.spellcraft.api;

/**
 * Represents the category a {@link Spell} belongs to.
 * Categories are used for organization, filtering, and presentation.
 */
public enum SpellCategory {

    /**
     * Spells that harness the power of elements.
     */
    ELEMENTAL("Elemental", "Spells that harness the power of elements"),

    /**
     * Offensive spells intended for combat.
     */
    COMBAT("Combat", "Offensive spells for battle"),

    /**
     * Utility spells that provide helpful effects.
     */
    UTILITY("Utility", "Helpful spells for various purposes"),

    /**
     * Spells that restore health or cure ailments.
     */
    HEALING("Healing", "Spells that restore health and cure ailments"),

    /**
     * Defensive spells such as shields or protective effects.
     */
    PROTECTION("Protection", "Defensive spells and shields"),

    /**
     * Spells focused on movement and travel.
     */
    TRANSPORTATION("Transportation", "Spells for movement and travel"),

    /**
     * Spells that are used for wide ranged attacks
     */
    OFFENSIVE("Offensive", "Spells used for wide range attacks"),

    /**
     * Spells that summon pets or creatures
     */
    SUMMONING("Summoning", "Spells that summon pets or creatures"),

    /**
     * ULTIMATE spells are powerful abilities with long cooldowns and high magic cost.
     * They often have unique effects and can turn the tide of battle.
     */
    ULTIMATE ("Ultimate", "Powerful spells with long cooldowns and high magic cost");

    private final String displayName;
    private final String description;

    SpellCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * @return the human-readable display name of this {@link SpellCategory}.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @return a short description explaining the purpose of this {@link SpellCategory}.
     */
    public String getDescription() {
        return description;
    }
}
