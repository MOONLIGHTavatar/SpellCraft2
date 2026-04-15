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
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class StoneCyclone extends AbstractSpell {

    private Location loc;

    public StoneCyclone() {
        super(
                "StoneCyclone",
                "A spinning cyclone of rocks that pulls enemies",
                SpellCategory.COMBAT,
                SpellCraftPlugin.getInstance().getConfig().getInt("spells.stonecyclone.magic-cost", 55),
                SpellCraftPlugin.getInstance().getConfig().getLong("spells.stonecyclone.cooldown", 9000),
                SpellCraftPlugin.getInstance().getConfig().getDouble("spells.stonecyclone.range", 20.0),
                SpellCraftPlugin.getInstance().getConfig().getBoolean("spells.stonecyclone.enabled", true),
                "Sneak to cast Stone Cyclone"
        );
    }

    @Override
    protected SpellResult execute(SpellCaster caster) {

        Player player = caster.getPlayer();
        loc = player.getLocation();

        double radius = SpellCraftPlugin.getInstance().getConfig().getDouble("spells.stonecyclone.pull-radius", 4.0);
        double speed = SpellCraftPlugin.getInstance().getConfig().getDouble("spells.stonecyclone.speed", 0.6);
        double damage = SpellCraftPlugin.getInstance().getConfig().getDouble("spells.stonecyclone.damage", 8.0);

        final double[] angle = {0};

        ThreadUtil.ensureLocationTimer(loc, () -> {

            angle[0] += 0.3;

            double x = Math.cos(angle[0]) * 2;
            double z = Math.sin(angle[0]) * 2;

            Location effectLoc = loc.clone().add(x, 1, z);

            ParticleEffect.BLOCK_CRACK.display(effectLoc, 10);

            for (LivingEntity e : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)
                    .stream().filter(en -> en instanceof LivingEntity).map(en -> (LivingEntity) en).toList()) {

                if (e.equals(player)) continue;

                Vector pull = loc.toVector().subtract(e.getLocation().toVector()).normalize().multiply(speed);
                e.setVelocity(pull);

                DamageHandler.damage(player, e, damage, getName(), getElement());
            }

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


    @Override public MagicElement getElement() { return MagicElement.EARTH; }
    @Override public boolean isSneakingAbility() { return true; }
    @Override public org.bukkit.event.block.Action getAbilityActivationAction() { return null; }

    @Override public @NotNull Location getLocation() { return loc; }
}