package com.spellcraft.spells;

import com.spellcraft.SpellCraftPlugin;
import com.spellcraft.api.SpellCaster;
import com.spellcraft.api.SpellCategory;
import com.spellcraft.api.SpellResult;
import com.spellcraft.api.magic.MagicElement;
import com.spellcraft.core.AbstractSpell;
import com.spellcraft.util.ThreadUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GolemSpell extends AbstractSpell {

    private Location currentLocation;
    private final List<IronGolem> summonedGolems = new ArrayList<>();

    public GolemSpell() {
        super(
                "Golem",
                "Summon 2 friendly Iron Golems to fight for you",
                SpellCategory.SUMMONING,
                SpellCraftPlugin.getInstance().getConfig().getInt("spells.golem.magic-cost", 120),
                SpellCraftPlugin.getInstance().getConfig().getLong("spells.golem.cooldown", 20000),
                10.0,
                SpellCraftPlugin.getInstance().getConfig().getBoolean("spells.golem.enabled", true),
                "Crouch to summon"
        );
    }

    @Override
    protected SpellResult execute(SpellCaster caster) {
        Player player = caster.getPlayer();
        currentLocation = player.getLocation().clone();

        for (int i = 0; i < 2; i++) {
            Location spawnLocation = currentLocation.clone().add((i == 0 ? 1 : -1), 0, 2);
            IronGolem golem = (IronGolem) spawnLocation.getWorld().spawn(spawnLocation, IronGolem.class);
            golem.setCustomName(player.getName() + "'s Golem " + (i + 1));
            golem.setCustomNameVisible(true);
            golem.setPlayerCreated(true);

            // Particle and sound effect
            spawnLocation.getWorld().spawnParticle(Particle.CRIT_MAGIC, spawnLocation, 50, 1, 1, 1);
            player.playSound(spawnLocation, Sound.ENTITY_IRON_GOLEM_STEP, 1f, 1.2f);

            summonedGolems.add(golem);

            // **Despawn golem after 1 minute**
            ThreadUtil.ensureLocationLater(spawnLocation, new Runnable() {
                @Override
                public void run() {
                    if (golem != null && !golem.isDead()) {
                        golem.remove();
                        summonedGolems.remove(golem);
                    }
                }
            }, 1200L); // 1200 ticks = 60 seconds
        }

        return SpellResult.SUCCESS;
    }

    @Override
    public void progress() {
        for (IronGolem golem : new ArrayList<>(summonedGolems)) {
            if (golem == null || golem.isDead()) continue;
            Player player = getLocation().getWorld().getPlayers().stream()
                    .filter(p -> golem.getCustomName() != null && golem.getCustomName().startsWith(p.getName()))
                    .findFirst().orElse(null);
            if (player != null && golem.getLocation().distance(player.getLocation()) > 5) {
                golem.teleport(player.getLocation());
            }
        }
    }

    @Override
    protected void onLoad() {}
    @Override
    protected void onStop() {
        for (IronGolem golem : summonedGolems) {
            if (golem != null && !golem.isDead()) golem.remove();
        }
        summonedGolems.clear();
    }

    @Override
    public boolean isSneakingAbility() { return true; }
    @Override
    public Action getAbilityActivationAction() { return Action.RIGHT_CLICK_AIR; }
    @Override
    public MagicElement getElement() { return MagicElement.EARTH; }
    @Override
    public @NotNull Location getLocation() {
        return currentLocation != null ? currentLocation.clone() : new Location(Bukkit.getWorlds().getFirst(), 0,0,0);
    }
}
