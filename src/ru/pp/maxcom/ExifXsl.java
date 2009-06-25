package ru.pp.maxcom;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Attr;

import java.io.File;
import java.io.IOException;

import com.drew.metadata.Metadata;
import com.drew.metadata.Directory;
import com.drew.metadata.MetadataException;
import com.drew.metadata.iptc.IptcDirectory;
import com.drew.metadata.exif.ExifDirectory;
import com.drew.metadata.exif.CanonMakernoteDirectory;
import com.drew.metadata.exif.CanonMakernoteDescriptor;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.lang.Rational;
import ru.org.linux.util.ImageInfo;
import ru.org.linux.util.BadImageException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class ExifXsl {
  public static final String EXIF_NS = "http://maxcom.pp.ru/photo/exif";
  final Directory exifDirectory;
  final ImageInfo info;
  private final Directory gpsDirectory;
  private final Directory iptcDirectory;

  public ExifXsl(String filename) throws JpegProcessingException, IOException, BadImageException {
    File jpegFile = new File(filename);
    Metadata metadata = JpegMetadataReader.readMetadata(jpegFile);
    exifDirectory = metadata.getDirectory(ExifDirectory.class);
    gpsDirectory = metadata.getDirectory(GpsDirectory.class);
    iptcDirectory = metadata.getDirectory(IptcDirectory.class);

    info = new ImageInfo(filename);
  }

  public Element getExif() throws  ParserConfigurationException, MetadataException {
    Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().getDOMImplementation().createDocument(EXIF_NS, "exif", null);

    Element camera = document.createElement("camera");
    camera.appendChild(document.createTextNode(exifDirectory.getString(ExifDirectory.TAG_MODEL)));
    document.getDocumentElement().appendChild(camera);

    Element exposure = document.createElement("exposure");
    exposure.setAttribute("aperture", exifDirectory.getString(ExifDirectory.TAG_FNUMBER));
    exposure.setAttribute("speed", exifDirectory.getString(ExifDirectory.TAG_EXPOSURE_TIME));
    exposure.setAttribute("iso", exifDirectory.getString(ExifDirectory.TAG_ISO_EQUIVALENT));

/*    int mode = exifDirectory.getInt(ExifDirectory.TAG_EXPOSURE_PROGRAM);
    switch (mode) {
      case 1:
        exposure.setAttribute("mode", "Manual");
        break;
      case 2:
        exposure.setAttribute("mode", "Program");
        break;
      case 3:
        exposure.setAttribute("mode", "Aperture priority (Av)");
        break;
      case 4:
        exposure.setAttribute("mode", "Shutter priority (Tv)");
        break;
      default:
    }
*/
    int flashMode = exifDirectory.getInt(ExifDirectory.TAG_FLASH);
    if ((flashMode & 1)==1) {
      exposure.setAttribute("flash", "TRUE");
    }

    document.getDocumentElement().appendChild(exposure);

    Element date = document.createElement("date");
    date.appendChild(document.createTextNode(exifDirectory.getDate(ExifDirectory.TAG_DATETIME_ORIGINAL).toString()));
    document.getDocumentElement().appendChild(date);

    Element lens = document.createElement("lens");
    lens.appendChild(document.createTextNode(exifDirectory.getString(ExifDirectory.TAG_FOCAL_LENGTH)));
    document.getDocumentElement().appendChild(lens);

/*    Element distance = document.createElement("distance");
    distance.appendChild(document.createTextNode(Double.toString(exifDirectory.getDouble((ExifDirectory.TAG_SUBJECT_DISTANCE)))));
    document.getDocumentElement().appendChild(distance);
*/

    if (gpsDirectory!=null && gpsDirectory.containsTag(GpsDirectory.TAG_GPS_LATITUDE)) {
      Element gps = document.createElement("gps");

      Rational[] lat = gpsDirectory.getRationalArray(GpsDirectory.TAG_GPS_LATITUDE);
      Rational[] lon = gpsDirectory.getRationalArray(GpsDirectory.TAG_GPS_LONGITUDE);

      double lattitude = lat[0].doubleValue() + lat[1].doubleValue()/60 + lat[2].doubleValue()/3600;
      double longitude = lon[0].doubleValue() + lon[1].doubleValue()/60 + lon[2].doubleValue()/3600;

      gps.setAttribute("alt", gpsDirectory.getRational(GpsDirectory.TAG_GPS_ALTITUDE).toSimpleString(true));
      gps.setAttribute("latitude", Double.toString(lattitude));
      gps.setAttribute("longitude", Double.toString(longitude));

      document.getDocumentElement().appendChild(gps);
    }

    if (iptcDirectory!=null) {
      Element iptc = document.createElement("iptc");

      String title = iptcDirectory.getString(IptcDirectory.TAG_OBJECT_NAME);
      if (title!=null && title.length()>0) {
        iptc.setAttribute("title", title);
      }

      document.getDocumentElement().appendChild(iptc);
    }

    return document.getDocumentElement();
  }

  public int getWidth() {
    return info.getWidth();
  }

  public int getHeight() {
    return info.getHeight();
  }
}
