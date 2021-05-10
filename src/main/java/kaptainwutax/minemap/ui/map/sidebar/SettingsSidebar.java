package kaptainwutax.minemap.ui.map.sidebar;

import kaptainwutax.biomeutils.biome.Biome;
import kaptainwutax.biomeutils.layer.BiomeLayer;
import kaptainwutax.biomeutils.source.LayeredBiomeSource;
import kaptainwutax.featureutils.Feature;
import kaptainwutax.minemap.MineMap;
import kaptainwutax.minemap.init.Configs;
import kaptainwutax.minemap.listener.Events;
import kaptainwutax.minemap.ui.component.BiomeEntry;
import kaptainwutax.minemap.ui.component.FeatureEntry;
import kaptainwutax.minemap.ui.map.MapPanel;
import kaptainwutax.minemap.ui.map.MapSettings;
import kaptainwutax.minemap.util.ui.interactive.Dropdown;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyBoundsAdapter;
import java.awt.event.HierarchyEvent;
import java.util.Arrays;
import java.util.stream.IntStream;

public class SettingsSidebar extends JPanel {

    private final MapPanel map;
    private final MapSettings settings;
    private final JPanel toggles = new JPanel();
    public Dropdown<Integer> layerDropdown;
    public JButton closeButton;
    public boolean isHiddenForSize = false;

    public SettingsSidebar(MapPanel map) {
        this.map = map;
        this.settings = this.map.getContext().getSettings();
        this.addLayerDropdown();
        this.addScrollPane();
        this.addGlobalToggles();
        this.addFeatureToggles();
        this.addBiomeToggles();
        this.addHideShowButtons();
        this.addSetResetButtons();
        this.addCloseButton();
        this.setLayout(new VerticalLayout());
    }

    private void addScrollPane() {
        this.toggles.setLayout(new GridLayout(0, 1));
        JScrollPane scrollPane = new JScrollPane(this.toggles);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);

        scrollPane.addHierarchyBoundsListener(new HierarchyBoundsAdapter() {
            @Override
            public void ancestorResized(HierarchyEvent e) {
                MapPanel map = MineMap.INSTANCE.worldTabs.getSelectedMapPanel();
                int height;
                if (map == null) {
                    height = MineMap.INSTANCE.getHeight() - 210;
                } else {
                    height = map.getHeight() - 150;
                }
                scrollPane.setPreferredSize(new Dimension(300, height));
                scrollPane.setSize(new Dimension(300, height));
                scrollPane.repaint();
            }
        });

        this.add(scrollPane);
    }

    private <T extends BiomeLayer> void addLayerDropdown() {
        LayeredBiomeSource<BiomeLayer> source = this.map.getContext().getBiomeSource();
        this.layerDropdown = new Dropdown<>(i -> "[" + i + "] " + source.getLayer(i).getClass().getSimpleName() + " " + source.getLayer(i).getScale() + ":1", IntStream.range(0, source.getLayerCount()).boxed());
        this.layerDropdown.selectIfPresent(this.map.getContext().getLayerId());

        this.layerDropdown.addActionListener(e1 -> {
            this.map.getContext().setLayerId(this.layerDropdown.getSelected());
            this.map.restart();
        });

        this.layerDropdown.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.add(this.layerDropdown);
    }

    private void addGlobalToggles() {
        JCheckBox showBiomes = new JCheckBox("Show Biomes") {
            @Override
            protected void paintComponent(Graphics g) {
                this.setSelected(SettingsSidebar.this.settings.showBiomes);
                super.paintComponent(g);
            }
        };

        JCheckBox showFeatures = new JCheckBox("Show Features") {
            @Override
            protected void paintComponent(Graphics g) {
                this.setSelected(SettingsSidebar.this.settings.showFeatures);
                super.paintComponent(g);
            }
        };

        JCheckBox showGrid = new JCheckBox("Show Grid") {
            @Override
            protected void paintComponent(Graphics g) {
                this.setSelected(SettingsSidebar.this.settings.showGrid);
                super.paintComponent(g);
            }
        };

        JCheckBox showExtraInfos = new JCheckBox("Show Extra infos") {
            @Override
            protected void paintComponent(Graphics g) {
                this.setSelected(SettingsSidebar.this.settings.showExtraInfos);
                super.paintComponent(g);
            }
        };

        JCheckBox showExtraIcons= new JCheckBox("Show Extra icons") {
            @Override
            protected void paintComponent(Graphics g) {
                this.setSelected(SettingsSidebar.this.settings.showExtraIcons);
                super.paintComponent(g);
            }
        };

        showBiomes.addItemListener(e -> {
            this.settings.showBiomes = showBiomes.isSelected();
            this.map.repaint();
        });

        showFeatures.addItemListener(e -> {
            this.settings.showFeatures = showFeatures.isSelected();
            this.map.repaint();
        });

        showGrid.addItemListener(e -> {
            this.settings.showGrid = showGrid.isSelected();
            this.map.repaint();
        });

        showExtraInfos.addItemListener(e -> {
            this.settings.showExtraInfos = showExtraInfos.isSelected();
            this.map.repaint();
        });

        showExtraIcons.addItemListener(e -> {
            this.settings.showExtraIcons = showExtraIcons.isSelected();
            this.map.repaint();
        });

        this.toggles.add(showBiomes);
        this.toggles.add(showFeatures);
        this.toggles.add(showGrid);
        this.toggles.add(showExtraInfos);
        this.toggles.add(showExtraIcons);
    }

    private void addFeatureToggles() {
        for (Feature<?, ?> feature : this.settings.getAllFeatures()) {
            FeatureEntry entry = new FeatureEntry(feature) {
                @Override
                public void paintComponent(Graphics g) {
                    this.getCheckBox().setSelected(SettingsSidebar.this.settings.isActive(feature));
                    super.paintComponent(g);
                }
            };

            entry.getCheckBox().setSelected(this.settings.isActive(feature));

            entry.getCheckBox().addItemListener(e -> {
                this.settings.setState(feature, entry.getCheckBox().isSelected());
                this.map.repaint();
            });

            this.toggles.add(entry);
        }
    }

    private void addBiomeToggles() {
        for (Biome biome : this.settings.getAllBiomes()) {
            BiomeEntry entry = new BiomeEntry(biome) {
                @Override
                public void paintComponent(Graphics g) {
                    this.getCheckBox().setSelected(SettingsSidebar.this.settings.isActive(biome));
                    super.paintComponent(g);
                }
            };

            entry.getCheckBox().setSelected(this.settings.isActive(biome));

            entry.getCheckBox().addItemListener(e -> {
                this.settings.setState(biome, entry.getCheckBox().isSelected());
                this.map.repaint();
                SwingUtilities.invokeLater(() -> this.settings.setDirty(false));
            });

            this.toggles.add(entry);
        }
    }

    private void addHideShowButtons() {
        JButton hideAll = new JButton("Hide All");
        JButton showAll = new JButton("Show All");

        hideAll.addMouseListener(Events.Mouse.onPressed(e -> {
            this.settings.getAllBiomes().forEach(this.settings::hide);
            this.settings.getAllFeatures().forEach(this.settings::hide);

            Arrays.stream(this.toggles.getComponents()).filter(c -> c instanceof FeatureEntry)
                .map(c -> (FeatureEntry) c).forEach(c -> c.getCheckBox().setSelected(false));
            Arrays.stream(this.toggles.getComponents()).filter(c -> c instanceof BiomeEntry)
                .map(c -> (BiomeEntry) c).forEach(c -> c.getCheckBox().setSelected(false));
            this.map.restart();
            this.map.repaint();
        }));

        showAll.addMouseListener(Events.Mouse.onPressed(e -> {
            this.settings.getAllBiomes().forEach(this.settings::show);
            this.settings.getAllFeatures().forEach(this.settings::show);

            Arrays.stream(this.toggles.getComponents()).filter(c -> c instanceof FeatureEntry)
                .map(c -> (FeatureEntry) c).forEach(c -> c.getCheckBox().setSelected(true));
            Arrays.stream(this.toggles.getComponents()).filter(c -> c instanceof BiomeEntry)
                .map(c -> (BiomeEntry) c).forEach(c -> c.getCheckBox().setSelected(true));
            this.map.restart();
            this.map.repaint();
        }));

        JPanel duo = new JPanel();
        duo.add(showAll);
        duo.add(hideAll);
        FlowLayout layout = new FlowLayout(FlowLayout.CENTER);
        layout.setVgap(0);
        duo.setLayout(layout);
        this.add(duo);
    }

    private void addSetResetButtons() {
        JButton set = new JButton("Set as Default");
        JButton reset = new JButton("Reset to Default");

        set.addMouseListener(Events.Mouse.onPressed(e -> {
            Configs.USER_PROFILE.setDefaultSettings(this.map.getContext().dimension, this.settings);
        }));

        reset.addMouseListener(Events.Mouse.onPressed(e -> {
            this.settings.set(Configs.USER_PROFILE.getMapSettingsCopy(this.map.getContext().version, this.map.getContext().dimension));
            this.map.repaint();
            this.toggles.repaint();
        }));

        JPanel duo = new JPanel();
        duo.add(set);
        duo.add(reset);
        FlowLayout layout = new FlowLayout(FlowLayout.CENTER);
        layout.setVgap(0);
        duo.setLayout(layout);
        this.add(duo);
    }

    private void addCloseButton() {
        JPanel panel = new JPanel();

        this.closeButton = new JButton("Close Settings");
        panel.add(this.closeButton);

        FlowLayout layout = new FlowLayout(FlowLayout.CENTER);
        panel.setLayout(layout);

        this.add(panel);
    }

}
