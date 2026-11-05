import java.awt.image.BufferedImage;
import java.io.File;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;

/**
 * Developer tool, not part of the build. Regenerates the MC 26.1 equipment textures from
 * Botania's 1.20.1 armour atlases.
 *
 * <p>Run with a JDK (>= 21) using single-file source launch:
 * <pre>{@code
 * java scripts/GenerateArmorEquipmentTextures.java \
 *   <path-to-minecraft-client-26.1.2.jar> \
 *   Xplat/src/main/resources/assets/botania/textures/model \
 *   Xplat/src/main/resources/assets/botania/textures/entity/equipment
 * }</pre>
 *
 * <p>Why this exists: the 1.20.1 atlases are 64x128 UV maps for Botania's custom
 * ArmorModel geometry (Forge {@code getArmorTexture} / Fabric {@code ArmorRenderer}).
 * MC 26.1 removed both hooks; {@code EquipmentClientInfo} now draws every armour piece
 * with the vanilla humanoid models, whose UV layout is 64x32 (64x64 for babies) and
 * completely different. This converter keeps the old atlas' Botania colours and pixel
 * detail, but clips it to the vanilla silhouette and applies the vanilla shading.
 * It therefore cannot reproduce the old custom 3D pieces (crystals, pauldrons, cloaks).
 */
public class GenerateArmorEquipmentTextures {
	private static final String[] MATERIALS = { "manasteel", "manaweave", "elementium", "terrasteel" };
	private static final String[] LAYERS = { "humanoid", "humanoid_leggings", "humanoid_baby" };

	public static void main(String[] args) throws Exception {
		if (args.length != 3) {
			System.err.println("usage: <minecraft client jar> <old atlas dir> <output equipment texture dir>");
			System.exit(2);
		}
		File jar = new File(args[0]);
		File oldDir = new File(args[1]);
		File outDir = new File(args[2]);
		try (ZipFile zip = new ZipFile(jar)) {
			for (String layer : LAYERS) {
				new File(outDir, layer).mkdirs();
			}
			for (String material : MATERIALS) {
				BufferedImage old = ImageIO.read(new File(oldDir, "armor_" + material + ".png"));
				int[] mean = meanColor(old);
				for (String layer : LAYERS) {
					String entry = "assets/minecraft/textures/entity/equipment/" + layer + "/iron.png";
					BufferedImage mask;
					try (var in = zip.getInputStream(zip.getEntry(entry))) {
						mask = ImageIO.read(in);
					}
					BufferedImage out = new BufferedImage(mask.getWidth(), mask.getHeight(), BufferedImage.TYPE_INT_ARGB);
					for (int y = 0; y < mask.getHeight(); y++) {
						for (int x = 0; x < mask.getWidth(); x++) {
							int m = mask.getRGB(x, y);
							int ma = m >>> 24;
							if (ma == 0) {
								continue;
							}
							int sy = sourceY(layer, y);
							int src = nearest(old, x, sy, mean);
							double lum = (((m >> 16) & 255) + ((m >> 8) & 255) + (m & 255)) / (3.0 * 255.0);
							double factor = 0.45 + 0.55 * lum;
							int r = clamp((int) (((src >> 16) & 255) * factor));
							int g = clamp((int) (((src >> 8) & 255) * factor));
							int b = clamp((int) ((src & 255) * factor));
							out.setRGB(x, y, (ma << 24) | (r << 16) | (g << 8) | b);
						}
					}
					File target = new File(outDir, layer + "/" + material + ".png");
					ImageIO.write(out, "png", target);
					System.out.println("wrote " + target + " " + out.getWidth() + "x" + out.getHeight());
				}
			}
		}
	}

	/** Upper body / legs in the old atlas are at y=0..31 and y=48..79 respectively. */
	private static int sourceY(String layer, int y) {
		return switch (layer) {
			case "humanoid" -> y;
			case "humanoid_leggings" -> y + 48;
			case "humanoid_baby" -> y < 32 ? y : (y - 32) + 48;
			default -> throw new IllegalArgumentException(layer);
		};
	}

	private static int nearest(BufferedImage src, int x, int y, int[] mean) {
		if (x >= 0 && x < src.getWidth() && y >= 0 && y < src.getHeight()) {
			int direct = src.getRGB(x, y);
			if ((direct >>> 24) != 0) {
				return direct;
			}
		}
		for (int radius = 1; radius <= 6; radius++) {
			int count = 0;
			long r = 0, g = 0, b = 0;
			for (int dy = -radius; dy <= radius; dy++) {
				for (int dx = -radius; dx <= radius; dx++) {
					if (Math.max(Math.abs(dx), Math.abs(dy)) != radius) {
						continue;
					}
					int sx = x + dx, sy = y + dy;
					if (sx < 0 || sx >= src.getWidth() || sy < 0 || sy >= src.getHeight()) {
						continue;
					}
					int p = src.getRGB(sx, sy);
					if ((p >>> 24) != 0) {
						r += (p >> 16) & 255;
						g += (p >> 8) & 255;
						b += p & 255;
						count++;
					}
				}
			}
			if (count > 0) {
				return (255 << 24) | ((int) (r / count) << 16) | ((int) (g / count) << 8) | (int) (b / count);
			}
		}
		return (255 << 24) | (mean[0] << 16) | (mean[1] << 8) | mean[2];
	}

	private static int[] meanColor(BufferedImage img) {
		long r = 0, g = 0, b = 0;
		int n = 0;
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				int p = img.getRGB(x, y);
				if ((p >>> 24) != 0) {
					r += (p >> 16) & 255;
					g += (p >> 8) & 255;
					b += p & 255;
					n++;
				}
			}
		}
		return n == 0 ? new int[] { 128, 128, 128 }
				: new int[] { (int) (r / n), (int) (g / n), (int) (b / n) };
	}

	private static int clamp(int v) {
		return Math.max(0, Math.min(255, v));
	}
}
