package com.spellcraft.spells;

import com.spellcraft.SpellCraftPlugin;
import com.spellcraft.api.SpellCategory;
import com.spellcraft.api.SpellCaster;
import com.spellcraft.api.SpellResult;
import com.spellcraft.api.magic.MagicElement;
import com.spellcraft.core.AbstractSpell;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class AngelFlareSpell extends AbstractSpell {

    private Location currentLocation;
    private final double beamRange = 20.0;
    private final double damagePerHit = 8.0;

    public AngelFlareSpell() {
        super(
                "Angel Flare",
                "Shoots a concentrated light beam wherever the crosshair points",
                SpellCategory.OFFENSIVE,
                SpellCraftPlugin.getInstance().getConfig().getInt("spells.angelflare.magic-cost", 60),
                SpellCraftPlugin.getInstance().getConfig().getLong("spells.angelflare.cooldown", 8000),
                null,
                SpellCraftPlugin.getInstance().getConfig().getBoolean("spells.angelflare.enabled", true),
                "Right Click while Sneaking"
        );
    }

    @Override
    protected SpellResult execute(SpellCaster caster) {
        Player player = caster.getPlayer();
        currentLocation = player.getEyeLocation().clone();
        Vector direction = player.getLocation().getDirection().normalize();

        for (double i = 0; i < beamRange; i += 0.5) {
            Location point = currentLocation.clone().add(direction.clone().multiply(i));
            point.getWorld().spawnParticle(Particle.END_ROD, point, 2, 0.1, 0.1, 0.1);
            point.getWorld().spawnParticle(Particle.FLASH, point, 1);

            for (Entity entity : point.getWorld().getNearbyEntities(point, 0.5, 0.5, 0.5)) {
                if (entity instanceof LivingEntity target && !target.equals(player)) {
                    target.damage(damagePerHit, player);
                    target.setVelocity(direction.clone().multiply(0.3));
                    point.getWorld().spawnParticle(Particle.CRIT_MAGIC, target.getLocation(), 5);
                    point.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 1.5f);
                }
            }
        }

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 1.2f);
        return SpellResult.SUCCESS;
    }

    @Override
    public void progress() {}

    @Override
    protected void onLoad() {}

    @Override
    protected void onStop() {}

    @Override
    public boolean isSneakingAbility() {
        return true;
    }

    @Override
    public Action getAbilityActivationAction() {
        return Action.RIGHT_CLICK_AIR;
    }

    @Override
    public MagicElement getElement() {
        return MagicElement.LIGHT;
    }

    @Override
    public @NotNull Location getLocation() {
        return currentLocation != null ? currentLocation.clone() : new Location(Bukkit.getWorlds().getFirst(), 0, 0, 0);
    }
}
