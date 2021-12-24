package me.saharnooby.qoi.plugin;

import lombok.NonNull;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageWriteParam;
import java.awt.*;

final class ImageParamUtil {

	static boolean isDefault(@NonNull ImageReadParam param) {
		return param.getClass() == ImageReadParam.class &&
				param.getSourceRegion() == null &&
				param.getSourceXSubsampling() == 1 &&
				param.getSourceYSubsampling() == 1 &&
				param.getSubsamplingXOffset() == 0 &&
				param.getSubsamplingYOffset() == 0 &&
				param.getSourceBands() == null &&
				param.getDestinationType() == null &&
				param.getDestinationOffset().equals(new Point(0, 0)) &&
				param.getDestination() == null &&
				param.getDestinationBands() == null;
	}

	static boolean isDefault(@NonNull ImageWriteParam param) {
		return param.getClass() == ImageWriteParam.class &&
				param.getSourceRegion() == null &&
				param.getSourceXSubsampling() == 1 &&
				param.getSourceYSubsampling() == 1 &&
				param.getSubsamplingXOffset() == 0 &&
				param.getSubsamplingYOffset() == 0 &&
				param.getSourceBands() == null &&
				param.getDestinationType() == null &&
				param.getDestinationOffset().equals(new Point(0, 0));
	}

}
