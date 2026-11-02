package vazkii.botania.api.configdata;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import org.jetbrains.annotations.Nullable;

public interface ConfigDataManager extends PreparableReloadListener {
	@Nullable
	LooniumStructureConfiguration getEffectiveLooniumStructureConfiguration(Identifier id);
}
