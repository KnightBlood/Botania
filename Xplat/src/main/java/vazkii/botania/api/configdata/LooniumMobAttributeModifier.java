package vazkii.botania.api.configdata;

import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class LooniumMobAttributeModifier {
	public static final Codec<LooniumMobAttributeModifier> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
					Codec.STRING.fieldOf("name").forGetter(mam -> mam.name),
					BuiltInRegistries.ATTRIBUTE.holderByNameCodec().fieldOf("attribute").forGetter(mam -> mam.attribute),
					Codec.DOUBLE.fieldOf("amount").forGetter(mam -> mam.amount),
					Codec.STRING.xmap(LooniumMobAttributeModifier::operationFromString,
							LooniumMobAttributeModifier::operationToString)
							.fieldOf("operation").forGetter(mam -> mam.operation)
			).apply(instance, LooniumMobAttributeModifier::new)
	);

	private final String name;
	public final Holder<Attribute> attribute;
	private final double amount;
	private final AttributeModifier.Operation operation;

	public LooniumMobAttributeModifier(String name, Holder<Attribute> attribute, double amount,
			AttributeModifier.Operation operation) {
		this.name = name;
		this.attribute = attribute;
		this.amount = amount;
		this.operation = operation;
	}

	public AttributeModifier createAttributeModifier() {
		return new AttributeModifier(Identifier.parse("botania:" + sanitizeName(name)), amount, operation);
	}

	/**
	 * 26.1 requires attribute modifier ids to be valid {@link Identifier}s. The 1.20.1 config
	 * names ("Loonium Modifier Health") predate that and are user-visible in the loonium config
	 * datapacks, so they are kept as-is there and normalized to a legal id path here.
	 */
	private static String sanitizeName(String name) {
		return name.toLowerCase(java.util.Locale.ROOT).replace(' ', '_').replaceAll("[^a-z0-9/._-]", "");
	}

	private static String operationToString(AttributeModifier.Operation operation) {
		return switch (operation) {
			case ADD_VALUE -> "addition";
			case ADD_MULTIPLIED_BASE -> "multiply_base";
			case ADD_MULTIPLIED_TOTAL -> "multiply_total";
			default -> throw new IllegalArgumentException("Unknown operation " + operation);
		};
	}

	private static AttributeModifier.Operation operationFromString(String operation) {
		return switch (operation) {
			case "addition" -> AttributeModifier.Operation.ADD_VALUE;
			case "multiply_base" -> AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
			case "multiply_total" -> AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
			default -> throw new JsonSyntaxException("Unknown attribute modifier operation " + operation);
		};
	}

	@Override
	public String toString() {
		return "MobAttributeModifier{" +
				"name='" + name + '\'' +
				", attribute=" + attribute +
				", amount=" + amount +
				", operation=" + operation +
				'}';
	}
}
