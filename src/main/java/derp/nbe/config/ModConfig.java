package derp.nbe.config;


import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "nbe")
public class ModConfig implements ConfigData {
    public boolean enabled = true;
}