package com.spellcraft.spells;

import com.spellcraft.SpellCraftPlugin;
import com.spellcraft.api.SpellCaster;
import com.spellcraft.api.SpellCategory;
import com.spellcraft.api.SpellResult;
import com.spellcraft.api.magic.MagicElement;
import com.spellcraft.core.AbstractSpell;
import com.spellcraft.util.DamageHandler;
import com.spellcraft.util.TempBlock;
import com.spellcraft.util.ThreadUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class WaterManipulation extends AbstractSpell {

    private Location currentLoc;
    private Location origin;
    private Vector direction;

    private TempBlock water;
    private Player player;

    private double speed;
    private double damage;
    private double hitRadius;
    private int maxTicks;
    private double range;

    private int lived;

    public WaterManipulation() {
        super(
                "WaterManipulation",
                "Shoot and control water in mid-air",
                SpellCategory.COMBAT,
                SpellCraftPlugin.getInstance().getConfig().getInt("spells.watermanipulation.magic-cost"),
                SpellCraftPlugin.getInstance().getConfig().getLong("spells.watermanipulation.cooldown"),
                SpellCraftPlugin.getInstance().getConfig().getDouble("spells.watermanipulation.range"),
                SpellCraftPlugin.getInstance().getConfig().getBoolean("spells.watermanipulation.enabled"),
                "Left click to launch, hold sneak to control"
        );
    }

    @Override
    protected SpellResult execute(SpellCaster caster) {

        player = caster.getPlayer();

        var config = SpellCraftPlugin.getInstance().getConfig();

        speed = config.getDouble("spells.watermanipulation.speed");
        damage = config.getDouble("spells.watermanipulation.damage");
        hitRadius = config.getDouble("spells.watermanipulation.hit-radius");
        maxTicks = config.getInt("spells.watermanipulation.max-ticks");
        range = getRange();

        origin = player.getEyeLocation();
        currentLoc = origin.clone();

        direction = origin.getDirection().normalize();

        Block block = currentLoc.getBlock();

        water = new TempBlock(block, Material.WATER);

        lived = 0;

        ThreadUtil.ensureLocationTimer(currentLoc, this::progressWater, 0, 1);

        return SpellResult.SUCCESS;
    }

    private void progressWater() {

        if (!player.isOnline() || player.isDead()) {
            remove();
            return;
        }

        if (lived++ > maxTicks) {
            remove();
            return;
        }

        if (origin.distanceSquared(currentLoc) > range * range) {
            remove();
            return;
        }

        if (player.isSneaking()) {
            direction = player.getEyeLocation().getDirection().normalize();
        }

        currentLoc.add(direction.clone().multiply(speed));

        Block block = currentLoc.getBlock();

        if (block.getType().isSolid()) {
            remove();
            return;
        }

        water.revert();
        water = new TempBlock(block, Material.WATER);

        for (var entity : currentLoc.getWorld().getNearbyEntities(currentLoc, hitRadius, hitRadius, hitRadius)) {

            if (!(entity instanceof LivingEntity living))
                continue;

            if (living.equals(player))
                continue;

            if (!DamageHandler.isValidTarget(player, living))
                continue;

            DamageHandler.damage(
                    player,
                    living,
                    damage,
                    getName(),
                    getElement()
            );

            remove();
            return;
        }
    }

    public void remove() {

        if (water != null && !water.isReverted()) {
            water.revert();
        }

        super.remove();
    }

    @Override
    public void progress() {
    }

    @Override
    protected void onLoad() {
    }

    @Override
    protected void onStop() {
        remove();
    }

    @Override
    public boolean isSneakingAbility() {
        return false;
    }

    @Override
    public Action getAbilityActivationAction() {
        return Action.LEFT_CLICK_AIR;
    }

    @Override
    public MagicElement getElement() {
        return MagicElement.WATER;
    }

    @Override
    public @NotNull Location getLocation() {
        return currentLoc == null ? origin : currentLoc;
    }
}
