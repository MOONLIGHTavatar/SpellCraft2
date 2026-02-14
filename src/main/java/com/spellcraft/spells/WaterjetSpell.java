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
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.block.Action;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class WaterjetSpell extends AbstractSpell {

    private Location currentLocation;

    public WaterjetSpell() {
        super(
                "Waterjet",
                "Shoots a fast orb of water at your opponent",
                SpellCategory.OFFENSIVE,
                SpellCraftPlugin.getInstance().getConfig().getInt("spells.waterjet.magic-cost", 30),
                SpellCraftPlugin.getInstance().getConfig().getLong("spells.waterjet.cooldown", 5000),
                null,
                SpellCraftPlugin.getInstance().getConfig().getBoolean("spells.waterjet.enabled", true),
                "Right Click while Sneaking"
        );
    }

    @Override
    protected SpellResult execute(SpellCaster caster) {
        Player player = caster.getPlayer();
        currentLocation = player.getEyeLocation().clone();

        Snowball waterOrb = player.launchProjectile(Snowball.class);
        waterOrb.setShooter(player);

        Vector direction = player.getLocation().getDirection().multiply(1.5);
        waterOrb.setVelocity(direction);

        Bukkit.getScheduler().runTaskTimer(SpellCraftPlugin.getInstance(), task -> {
            if (waterOrb.isDead() || !waterOrb.isValid()) {
                task.cancel();
                return;
            }
            waterOrb.getWorld().spawnParticle(Particle.WATER_SPLASH, waterOrb.getLocation(), 5, 0.2, 0.2, 0.2);
        }, 0L, 1L);

        player.playSound(currentLocation, Sound.ENTITY_SPLASH_POTION_THROW, 1.0f, 1.2f);

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
        return MagicElement.WATER;
    }

    @Override
    public @NotNull Location getLocation() {
        return currentLocation != null ? currentLocation.clone() : new Location(Bukkit.getWorlds().getFirst(), 0, 0, 0);
    }
}
