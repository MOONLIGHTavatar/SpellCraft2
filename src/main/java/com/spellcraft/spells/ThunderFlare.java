package com.spellcraft.spells;

import com.spellcraft.SpellCraftPlugin;
import com.spellcraft.api.*;
import com.spellcraft.api.magic.MagicElement;
import com.spellcraft.core.AbstractSpell;
import com.spellcraft.util.DamageHandler;
import com.spellcraft.util.ParticleEffect;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.jetbrains.annotations.NotNull;

public class ThunderFlare extends AbstractSpell {

    private Location loc;

   public ThunderFlare() {
    super(
            "Thunderflare",
            "Lightning strike that explodes into fire",
            SpellCategory.COMBAT,
            SpellCraftPlugin.getInstance().getConfig().getInt("spells.thunderflare.magic-cost", 45),
            SpellCraftPlugin.getInstance().getConfig().getLong("spells.thunderflare.cooldown", 8000),
            SpellCraftPlugin.getInstance().getConfig().getDouble("spells.thunderflare.range", 30.0),
            SpellCraftPlugin.getInstance().getConfig().getBoolean("spells.thunderflare.enabled", true),
            "Sneak to cast Thunderflare"
    );
}

    @Override
    protected SpellResult execute(SpellCaster caster) {

        Player player = caster.getPlayer();

        loc = player.getTargetBlock(null, 30).getLocation().add(0.5,0.5,0.5);

        loc.getWorld().strikeLightningEffect(loc);

        ParticleEffect.FLAME.display(loc, 40);
        ParticleEffect.EXPLOSION_LARGE.display(loc, 2);

        loc.getWorld().getNearbyEntities(loc,3,3,3).forEach(e -> {

            if (!(e instanceof LivingEntity living)) return;
            if (living.equals(player)) return;

            DamageHandler.damage(player, living, 12, getName(), getElement());
            living.setFireTicks(60);
        });

        return SpellResult.SUCCESS;
    }

    @Override public MagicElement getElement() { return MagicElement.FIRE; }
    @Override public boolean isSneakingAbility() { return true; }
    @Override public Action getAbilityActivationAction() { return null; }

    @Override
    public @NotNull Location getLocation() { return loc; }

    @Override
    public void onLoad() {
    }

    @Override
    public void progress() {
    }

    @Override
    public void onStop() {
    }
}