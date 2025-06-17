package com.zzimple.estimate.coordinate;

import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;

public class CoordinateConverter {

  private static final CoordinateTransform transform;

  static {
    CRSFactory crsFactory = new CRSFactory();
    CoordinateReferenceSystem srcCrs = crsFactory.createFromName("EPSG:5179"); // 도로명 주소 TM 좌표
    CoordinateReferenceSystem dstCrs = crsFactory.createFromName("EPSG:4326"); // 위경도 (WGS84)
    CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
    transform = ctFactory.createTransform(srcCrs, dstCrs);
  }

  public static double[] convertToWGS84(double entX, double entY) {
    ProjCoordinate srcCoord = new ProjCoordinate(entX, entY);
    ProjCoordinate dstCoord = new ProjCoordinate();
    transform.transform(srcCoord, dstCoord);
    return new double[]{dstCoord.y, dstCoord.x}; // [lat, lng]
  }
}