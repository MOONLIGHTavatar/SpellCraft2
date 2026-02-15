<div align="center">
✨ SpellCraft2 🎇  
A modern magic system for Minecraft servers  

Paper • Spigot • Folia • Extensible • High Performance
</div>

---

🌟 **About**

SpellCraft2 is a powerful, extensible magic plugin designed for modern Minecraft servers.  
It builds on the original SpellCraft with:

- New spells  
- Perks & progression  
- Folia-safe performance  
- Developer-friendly API  

Perfect for RPG servers, fantasy worlds, and large-scale multiplayer servers.

---

🚀 **Features**

### 🪄 Core Magic System
- Mana-based casting with regeneration  
- Action bar magic UI  
- Configurable cooldowns  
- Permission-based spell access  

### 🔥 Built-in Spells
Includes a growing spell library:  

- 🔥 Fireball  
- ⚡ Lightning  
- ❤️ Heal  
- 🌀 Teleport  
- 🛡️ Shield  
- ❄️ Elemental magic  

Each spell supports:  
- Mana cost  
- Cooldowns  
- Permissions  
- Custom configs  

### 📖 Spellbooks
Unlock magic through progression:  
- Craftable spellbooks  
- World-generated loot  
- Command distribution  
- Configurable progression  

### 📊 Perks System
Permanent upgrades via `plugins/SpellCraft2/perks.yml`. Examples:  
- Faster mana regen  
- Reduced cooldowns  
- Passive buffs  
- RPG-style builds  

### 🧵 Folia Ready
Fully thread-safe:  
- Region scheduler support  
- Async-safe casting  
- High scalability  
- Future-proof architecture  

### 🔌 Developer API
Easily create addons:  
- Custom spells  
- Custom perks  
- Mana hooks  
- Event integrations  

---

📦 **Installation**

1️⃣ **Download**  
Get the latest `SpellCraft2.jar`  

2️⃣ **Install**  
Place inside `/plugins/`  

3️⃣ **First Launch**  
Start the server once to generate configs:  
- `plugins/SpellCraft2/config.yml`  
- `plugins/SpellCraft2/perks.yml`  

4️⃣ **Configure**  
Edit configs → Restart server ✅  

---

⚙️ **Compatibility**
- ✔ Paper 1.20+  
- ✔ Spigot 1.20+  
- ✔ Folia Supported  
- ⚠ Bukkit (Not Recommended)  

---

🧙 **Commands**
- **Main:** `/spell <subcommand>`  
- **Casting:** `/spell cast <spell>`  
- **Spellbooks:** `/spell give <player> <spellbook>`  
- **Perks:** `/spell perks`  
- **Admin:** `/spell reload`  

---

🔐 **Permissions**

- **spellcraft2.** `All permissions`
- **spellcraft2.command.** `Commands`
- **spellcraft2.spell.**  `Spells`
- **spellcraft2.perks.** `Perks`

- **spellcraft2.admin** `All permissions.`
- **spellcraft2.reload** `Reload`



---

⚡ **Mana System**
Flexible and configurable:  
- Regen over time  
- Spell costs  
- Scalable balancing  
- RPG-ready design  

Edit in `config.yml`.

---

🛠️ **Configuration**
Main file: `plugins/SpellCraft2/config.yml`  
Customize:  
- Mana values  
- Spell cooldowns  
- UI behavior  
- Enabled features  
- Balance settings  

---

🔌 **Soft Dependencies**
Optional integrations:  
- WorldGuard  
- GriefPrevention  

Adds region-safe spellcasting.

---

🧑‍💻 **Developer API**

Example usage:

```java
Spell spell = new SpellBuilder("FireNova")
    .manaCost(25)
    .cooldown(10)
    .onCast(player -> {
        player.getWorld().createExplosion(player.getLocation(), 2F);
    })
    .build();

SpellRegistry.register(spell);


