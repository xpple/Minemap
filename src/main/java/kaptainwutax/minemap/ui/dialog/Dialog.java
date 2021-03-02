package kaptainwutax.minemap.ui.dialog;

import kaptainwutax.minemap.MineMap;
import kaptainwutax.minemap.init.Configs;
import kaptainwutax.minemap.ui.component.TabHeader;
import kaptainwutax.minemap.ui.map.MapPanel;
import kaptainwutax.seedutils.mc.Dimension;
import kaptainwutax.seedutils.mc.MCVersion;
import wearblackallday.swing.SwingUtils;
import wearblackallday.swing.components.CustomPanel;
import wearblackallday.swing.components.SelectionBox;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;

public abstract class Dialog extends JDialog {

	public Dialog(String title) {
		this.setTitle(title);
		this.setAlwaysOnTop(true);
		this.setModalityType(ModalityType.APPLICATION_MODAL);
		this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		this.setVisible(false);
	}

	public void format() {
		this.pack();
		this.setLocationRelativeTo(null);
	}

	public static class CoordHopperDialogue extends Dialog {

		public CustomPanel customPanel;
		public CustomPanel.Key<SelectionBox<Type>> typeSelection;

		public CoordHopperDialogue() {
			super("Go to Coordinates");
			JButton continueButton = new JButton("Continue");
			continueButton.addActionListener(e -> {
				try {
					int x, z;
					x = Integer.parseInt(this.customPanel.getText("x").trim());
					z = Integer.parseInt(this.customPanel.getText("z").trim());
					x = this.customPanel.getComponent(this.typeSelection).getSelected().transform(x);
					z = this.customPanel.getComponent(this.typeSelection).getSelected().transform(z);
					MapPanel map = MineMap.INSTANCE.worldTabs.getSelectedMapPanel();
					if (map != null) map.getManager().setCenterPos(x, z);
					this.setVisible(false);
				} catch (NumberFormatException ignored) {
				}
			});
			this.getRootPane().setDefaultButton(continueButton);
			this.setContentPane(this.customPanel = new CustomPanel(new GridLayout(0, 2), 70, 40).
					addTextField("X Coordinate...", "x").
					addTextField("Z Coordinate...", "z").
					addComponent(this.typeSelection, () -> new SelectionBox<>(Type::getName, Type.values())).
					addComponent(() -> continueButton));
			this.format();
		}

		protected enum Type {
			BLOCK("Block Coordinates", i -> i),
			CHUNK("Chunk Coordinates", i -> i << 4),
			REGION_32("Chunk Region Coordinates (32x32)", i -> CHUNK.transform(i) << 5);

			private final String name;
			private final IntUnaryOperator transformation;

			Type(String name, IntUnaryOperator transformation) {
				this.name = name;
				this.transformation = transformation;
			}

			public String getName() {
				return this.name;
			}

			public int transform(int i) {
				return this.transformation.applyAsInt(i);
			}
		}
	}

	public static class EnterSeedDialog extends Dialog {

		private final CustomPanel customPanel;
		private final CustomPanel.Key<SelectionBox<Integer>> threadSelection = new CustomPanel.Key<>();
		private final CustomPanel.Key<SelectionBox<MCVersion>> versionSelection = new CustomPanel.Key<>();

		public EnterSeedDialog() {
			super("Load new Seed");
			int cores = Runtime.getRuntime().availableProcessors();
			this.setContentPane(this.customPanel = new CustomPanel(new GridLayout(3, 1), 80, 30).
					addTextField("Enter your seed here", "seed").
					addComponent(this.threadSelection, () -> new SelectionBox<>(
							i -> i + (i == 1 ? " thread" : " threads"), IntStream.rangeClosed(1, cores).boxed())).
					addComponent(this.versionSelection, () -> new SelectionBox<>(
							Arrays.stream(MCVersion.values()).filter(v -> v.isNewerOrEqualTo(MCVersion.v1_8)))));
			SwingUtils.addSet(this.customPanel, Arrays.stream(Dimension.values()).map(dimension -> {
				String s = Character.toUpperCase(dimension.getName().charAt(0)) + dimension.getName().substring(1);
				JCheckBoxMenuItem check = new JCheckBoxMenuItem("Load " + s);
				check.setState(Configs.USER_PROFILE.isDimensionEnabled(dimension));
				check.addChangeListener(e -> Configs.USER_PROFILE.setDimensionState(dimension, check.getState()));
				return check;
			}).toArray(JCheckBoxMenuItem[]::new));
			JButton continueButton = new JButton("Continue");
			this.getRootPane().setDefaultButton(continueButton);
			continueButton.addActionListener(e -> {
				int usedCores = this.customPanel.getComponent(this.threadSelection).getSelected();
				MCVersion ver = this.customPanel.getComponent(this.versionSelection).getSelected();
				MineMap.INSTANCE.worldTabs.load(ver,
						this.customPanel.getText("seed").trim(),
						usedCores, Configs.USER_PROFILE.getEnabledDimensions());
				Configs.USER_PROFILE.setThreadCount(usedCores);
				Configs.USER_PROFILE.setVersion(ver);
				this.setVisible(false);
			});
			SwingUtils.addSet(this, new JLabel(), continueButton);
			this.format();
		}
	}

	public static class RenameTabDialog extends Dialog {

		public RenameTabDialog() {
			super("Rename Tab");
			CustomPanel customPanel;
			JButton continueButton = new JButton("Continue");

			this.getRootPane().setDefaultButton(continueButton);
			this.setContentPane(customPanel = new CustomPanel(new GridLayout(2, 0) ,200, 40).
					addTextField("Enter your Tab name", "head").
					addComponent(() -> continueButton));
			continueButton.addActionListener(e -> {
				TabHeader header = MineMap.INSTANCE.worldTabs.getSelectedHeader();
				header.setName(customPanel.getText("head"));
				this.setVisible(false);
			});
			this.format();
		}
	}

}
