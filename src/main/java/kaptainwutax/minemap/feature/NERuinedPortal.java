package kaptainwutax.minemap.feature;

import kaptainwutax.featureutils.structure.RuinedPortal;
import kaptainwutax.seedutils.mc.Dimension;
import kaptainwutax.seedutils.mc.MCVersion;

public class NERuinedPortal extends RuinedPortal {

	public NERuinedPortal(MCVersion version) {
		super(Dimension.NETHER,version);
	}

	public NERuinedPortal(Config config, MCVersion version) {
		super(Dimension.NETHER,config, version);
	}

	@Override
	public String getName() {
		return "NE_RuinedPortal";
	}

	@Override
	public boolean isValidDimension(Dimension dimension) {
		return dimension == Dimension.NETHER;
	}

}
