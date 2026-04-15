package com.spellcraft.spells;

import com.sk89q.wepif.bPermissionsResolver;
import com.spellcraft.SpellCraftPlugin;
import com.spellcraft.api.*;
import com.spellcraft.api.magic.MagicElement;
import com.spellcraft.core.AbstractSpell;
import com.spellcraft.util.DamageHandler;
import com.spellcraft.util.ParticleEffect;
import com.spellcraft.util.ThreadUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class StormIgnition extends AbstractSpell {

    private Location loc;

    public StormIgnition() {
        super(
                "StormIgnition",
                "Lightning orb that burns the ground",
                SpellCategory.COMBAT,
                SpellCraftPlugin.getInstance().getConfig().getInt("spells.stormignition.magic-cost", 50),
                SpellCraftPlugin.getInstance().getConfig().getLong("spells.stormignition.cooldown", 9000),
                SpellCraftPlugin.getInstance().getConfig().getDouble("spells.stormignition.range", 35.0),
                SpellCraftPlugin.getInstance().getConfig().getBoolean("spells.stormignition.enabled", true),
                "Sneak to cast Storm Ignition"
        );
    }

    @Override
    protected SpellResult execute(SpellCaster caster) {

        Player player = caster.getPlayer();

        double speed = SpellCraftPlugin.getInstance().getConfig().getDouble("spells.stormignition.speed", 0.75);
        double radius = SpellCraftPlugin.getInstance().getConfig().getDouble("spells.stormignition.hit-radius", 2.5);
        double damage = SpellCraftPlugin.getInstance().getConfig().getDouble("spells.stormignition.damage", 12.0);

        loc = player.getEyeLocation().clone();
        Vector dir = loc.getDirection().normalize();

        ThreadUtil.ensureLocationTimer(loc, () -> {

            loc.add(dir.multiply(speed));

            ParticleEffect.FLAME.display(loc, 10);
            ParticleEffect.FLAME.display(loc, 10);

            loc.getBlock().getRelative(0, -1, 0).setType(Material.FIRE);

            loc.getWorld().getNearbyEntities(loc, radius, radius, radius).forEach(e -> {
                if (!(e instanceof LivingEntity living)) return;
                if (living.equals(player)) return;

                DamageHandler.damage(player, living, damage, getName(), getElement());
                living.setFireTicks(60);
            });

        }, 0, 1);

        return SpellResult.SUCCESS;
    }

    @Override public MagicElement getElement() { return MagicElement.LIGHTNING; }
    @Override public boolean isSneakingAbility() { return true; }
    @Override public Action getAbilityActivationAction() { return null; }

    @Override public @NotNull Location getLocation() { return loc; }

    @Override
    public void onLoad() { }

    @Override
    public void progress() { }

    @Override
    public void onStop() { }
}