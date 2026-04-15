package com.spellcraft.spells;

import com.spellcraft.SpellCraftPlugin;
import com.spellcraft.api.*;
import com.spellcraft.api.magic.MagicElement;
import com.spellcraft.core.AbstractSpell;
import com.spellcraft.util.DamageHandler;
import com.spellcraft.util.ParticleEffect;
import com.spellcraft.util.ThreadUtil;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class VolcanicSurge extends AbstractSpell {

    private Location loc;

    public VolcanicSurge() {
        super(
                "VolcanicSurge",
                "A rushing fire wave",
                SpellCategory.COMBAT,
                SpellCraftPlugin.getInstance().getConfig().getInt("spells.volcanicsurge.magic-cost", 40),
                SpellCraftPlugin.getInstance().getConfig().getLong("spells.volcanicsurge.cooldown", 7000),
                SpellCraftPlugin.getInstance().getConfig().getDouble("spells.volcanicsurge.range", 25.0),
                SpellCraftPlugin.getInstance().getConfig().getBoolean("spells.volcanicsurge.enabled", true),
                "Sneak to cast Volcanic Surge"
        );
    }

    @Override
    protected SpellResult execute(SpellCaster caster) {

        Player player = caster.getPlayer();

        double speed = SpellCraftPlugin.getInstance().getConfig().getDouble("spells.volcanicsurge.speed", 0.8);
        double radius = SpellCraftPlugin.getInstance().getConfig().getDouble("spells.volcanicsurge.hit-radius", 2.0);
        double damage = SpellCraftPlugin.getInstance().getConfig().getDouble("spells.volcanicsurge.damage", 10.0);
        int burn = SpellCraftPlugin.getInstance().getConfig().getInt("spells.volcanicsurge.burn-time", 80);

        loc = player.getEyeLocation().clone();
        Vector dir = loc.getDirection().normalize();

        ThreadUtil.ensureLocationTimer(loc, () -> {

            loc.add(dir.multiply(speed));

            ParticleEffect.FLAME.display(loc, 25);
            ParticleEffect.LAVA.display(loc, 5);

            loc.getWorld().getNearbyEntities(loc, radius, radius, radius).forEach(e -> {
                if (!(e instanceof LivingEntity living)) return;
                if (living.equals(player)) return;

                DamageHandler.damage(player, living, damage, getName(), getElement());
                living.setFireTicks(burn);
            });

        }, 0, 1);

        return SpellResult.SUCCESS;
    }

    @Override
    public void onLoad() {
    }

    @Override
    public void progress() {
    }

    @Override
    public void onStop() {
    }

    @Override public MagicElement getElement() { return MagicElement.FIRE; }
    @Override public boolean isSneakingAbility() { return true; }
    @Override public Action getAbilityActivationAction() { return null; }

    @Override public @NotNull Location getLocation() { return loc; }
}