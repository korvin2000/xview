package net.j7.commons.net;
/**
 * Constants for MimeType enumeration
 *
 * @author  <a href="delafer2000@mail.ru"> tavrovsa</a>
 * @see <a href="http://www.iana.org/assignments/media-types">Registered IANA Media Types List</a>
 *
 */
public interface MimeConstants {

	public static final String MIME_TYPE_DOC_AI = "application/postscript";
	public static final String MIME_TYPE_DOC_CSV = "text/csv";
	public static final String MIME_TYPE_DOC_DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
	public static final String MIME_TYPE_DOC_MSWORD = "application/msword";
	public static final String MIME_TYPE_DOC_RTF = "application/rtf";
	public static final String MIME_TYPE_MS_EXCEL_XLS = "application/vnd.ms-excel";
	public static final String MIME_TYPE_MS_EXCEL_2007 = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	public static final String MIME_TYPE_DOC_PDF = "application/pdf";
	public static final String MIME_TYPE_TXT = "text/plain";
	public static final String MIME_TYPE_IMG_GIF = "image/gif";
	public static final String MIME_TYPE_IMG_JPEG = "image/jpeg";
	public static final String MIME_TYPE_IMG_PNG = "image/png";
	public static final String MIME_TYPE_IMG_BMP = "image/bmp";
	public static final String MIME_TYPE_VIDEO_AVI = "video/x-msvideo";
	public static final String MIME_TYPE_VIDEO_QUICKTIME = "image/png";
	public static final String MIME_TYPE_VIDEO_MPEG = "video/quicktime";
	public static final String MIME_TYPE_VIDEO_FLASH = "video/x-flv";
	public static final String MIME_TYPE_AUDIO_MP3 = "audio/mpeg";
	public static final String MIME_TYPE_OCTET_STREAM = "application/octet-stream";
	public static final String MIME_TYPE_CLASS = "application/java-vm";
	public static final String MIME_TYPE_JAVASCRIPT = "application/javascript";
	public static final String MIME_TYPE_JAVA = "text/x-java-source";
	public static final String MIME_TYPE_JAVA_SERIALIZED = "application/java-serialized-object";
	public static final String MIME_TYPE_WAR_ARCHIVE = "application/java-archive";
	public static final String MIME_TYPE_JAR_ARCHIVE = "application/java-archive";
	public static final String MIME_TYPE_CSS = "text/css";
	public static final String MIME_TYPE_FLASH = "application/x-shockwave-flash";
	public static final String MIME_TYPE_FONT_TTF = "application/x-font-ttf";
	public static final String MIME_TYPE_JAXRS_WILDCARD_TYPE = "*/*";
	public static final String MIME_TYPE_JSON = "application/json";
	public static final String MIME_TYPE_XML = "text/xml";
	public static final String MIME_TYPE_XML_DTD = "application/xml-dtd";
	public static final String MIME_TYPE_SVG_XML = "application/svg+xml";
	public static final String MIME_TYPE_HTML = "text/html";
	public static final String MIME_TYPE_XHTML = "application/xhtml+xml";
	public static final String MIME_TYPE_ATOM_FEEDS_XML = "application/atom+xml";
	public static final String MIME_TYPE_PROTOCOL_SOAP = "application/soap+xml";
	public static final String MIME_TYPE_ZIP = "application/zip";
	public static final String MIME_TYPE_GZIP = "application/gzip";
	public static final String MIME_TYPE_BZIP = "application/x-bzip";
	public static final String MIME_TYPE_BZIP2 = "application/x-bzip2";
	public static final String MIME_TYPE_TAR = "application/x-tar";
	public static final String MIME_TYPE_RAR = "application/x-rar-compressed";
	public static final String MIME_TYPE_7ZIP = "application/x-7z-compressed";
	public static final String MIME_TYPE_MODEL_VRML = "model/vrml";
	public static final String MIME_TYPE_MESSAGE_HTTP = "message/http";
	public static final String MIME_TYPE_MESSAGE_PARTIAL = "message/partial";
	public static final String MIME_TYPE_MESSAGE_RFC822 = "message/rfc822";
	public static final String MIME_TYPE_MULTIPART_FORM_DATA = "multipart/form-data";
	public static final String MIME_TYPE_MULTIPART_MIXED = "multipart/mixed";
	public static final String MIME_TYPE_MULTIPART_SIGNED = "multipart/signed";
	public static final String MIME_TYPE_MULTIPART_ENCRYPTED = "multipart/encrypted";
	public static final String MIME_TYPE_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";

	public enum MimeType implements MimeConstants {



		/* Documents */
		/** Adobe Illustrator document */
		TYPE_DOC_AI (MIME_TYPE_DOC_AI, "ai;eps;ps", "Adobe Illustrator document", ContentType.Application, ContentType.Document),
		/** Comma-separated values (RFC 4180) */
		TYPE_DOC_CSV (MIME_TYPE_DOC_CSV, "csv", "Comma-separated values (RFC 4180)", ContentType.Text, ContentType.Document),
		/** OpenXML Office Document */
		TYPE_DOC_DOCX (MIME_TYPE_DOC_DOCX, "docx", "OpenXML Office Document", ContentType.Application, ContentType.Document),
		/** MS Word document */
		TYPE_DOC_MSWORD (MIME_TYPE_DOC_MSWORD, "doc;dot", "MS Word document", ContentType.Application, ContentType.Document),
		/** Rich Text Format */
		TYPE_DOC_RTF (MIME_TYPE_DOC_RTF, "rtf", "Rich Text Format", ContentType.Application, ContentType.Document),
		/** MS Excel 2003 XLS document */
		TYPE_MS_EXCEL_XLS (MIME_TYPE_MS_EXCEL_XLS, "xls", "MS Excel XLS document", ContentType.Application, ContentType.Document),
		/** MS Excel > 2007 XLSX document */
		TYPE_MS_EXCEL_2007 (MIME_TYPE_MS_EXCEL_2007, "xlsx", "MS Excel 2007 document", ContentType.Application, ContentType.Document),

		/** Adobe Portable Document Format (PDF) */
		TYPE_DOC_PDF (MIME_TYPE_DOC_PDF, "pdf", "Adobe Portable Document Format", ContentType.Application, ContentType.Document),
		/** Plain Text Document */
		TYPE_TXT (MIME_TYPE_TXT, "txt", "Plain text format", ContentType.Text, ContentType.Document),

		/* Images */
		/** GIF Image Format  */
		TYPE_IMG_GIF (MIME_TYPE_IMG_GIF, "gif", "GIF Image Format", ContentType.Image),
		/** Jpeg Image format */
		TYPE_IMG_JPEG (MIME_TYPE_IMG_JPEG, "jpg;jpeg;jpe", "Jpeg Image format", ContentType.Image),
		/** Portable Network Graphic format */
		TYPE_IMG_PNG (MIME_TYPE_IMG_PNG, "png", "Portable Network Graphic format", ContentType.Image),
		/** MS BMP Graphic format" */
		TYPE_IMG_BMP (MIME_TYPE_IMG_BMP, "bmp", "MS BMP Graphic format", ContentType.Image),

		/* Video */
		/** MS AVI video format container */
		TYPE_VIDEO_AVI (MIME_TYPE_VIDEO_AVI, "avi", "MS AVI video format container", ContentType.Video),
		/** Apple QuickTime Video */
		TYPE_VIDEO_QUICKTIME (MIME_TYPE_VIDEO_QUICKTIME, "qt;mov", "Apple QuickTime Video", ContentType.Video),
		/** MPEG Video format */
		TYPE_VIDEO_MPEG (MIME_TYPE_VIDEO_MPEG, "mpg;mpeg;mpe", "MPEG Video format", ContentType.Video),
		/** FLV / Flash video format */
		TYPE_VIDEO_FLASH (MIME_TYPE_VIDEO_FLASH, "flv", "FLV / Flash video format", ContentType.Video),

		/* Audio */
		/** Audio Mpeg Layer 3 Format */
		TYPE_AUDIO_MP3 (MIME_TYPE_AUDIO_MP3, "mp3", "Audio Mpeg Layer 3 Format", ContentType.Audio),

		/* other formats */
		/** Application Octet Stream - uninterpreted binary data */
		TYPE_OCTET_STREAM (MIME_TYPE_OCTET_STREAM, null, " Application Octet Stream - uninterpreted binary data", ContentType.Application),

		/** JAVA JVM Class / Bytecode file */
		TYPE_CLASS (MIME_TYPE_CLASS, "class", "java class", ContentType.Application),
		/** Java Script */
		TYPE_JAVASCRIPT (MIME_TYPE_JAVASCRIPT, "js", "Java Script", ContentType.Application),
		/** Java source code */
		TYPE_JAVA (MIME_TYPE_JAVA, "java", "Java source code", ContentType.Text),
		/** Java serialized object */
		TYPE_JAVA_SERIALIZED (MIME_TYPE_JAVA_SERIALIZED, "ser", "Java serialized object", ContentType.Application),
		/**  Web application ARchive (WAR) */
		TYPE_WAR_ARCHIVE (MIME_TYPE_WAR_ARCHIVE, "war", "Web application ARchive (WAR)", ContentType.Application, ContentType.Archive),
		/**  Java application ARchive (JAR) */
		TYPE_JAR_ARCHIVE (MIME_TYPE_JAR_ARCHIVE, "jar", "Java application ARchive (JAR)", ContentType.Application, ContentType.Archive),
		/** Cascading Style Sheets (CSS) */
		TYPE_CSS (MIME_TYPE_CSS, "css", "Cascading Style Sheets", ContentType.Text),

		/** Adobe Shockwave flash */
		TYPE_FLASH(MIME_TYPE_FLASH, "swf", "Shockwave flash", ContentType.Application),

		/** TrueType Font */
		TYPE_FONT_TTF(MIME_TYPE_FONT_TTF, "ttf", "TrueType Font", ContentType.Application),

		/** JAX-RS Specific javax.ws.rs.core.MediaType.WILDCARD_TYPE */
		TYPE_JAXRS_WILDCARD_TYPE(MIME_TYPE_JAXRS_WILDCARD_TYPE, "*", "JAX-RS Specific javax.ws.rs.core.MediaType.WILDCARD_TYPE", new ContentType[]{}),

		/* XML-Like/structured text formats */
		/** The application/json Media Type for JavaScript Object Notation */
		TYPE_JSON (MIME_TYPE_JSON, "json", "The application/json Media Type for JavaScript Object Notation", ContentType.Application),
		/** Extensible Markup Language (XML) */
		TYPE_XML (MIME_TYPE_XML, "xml", "Extensible Markup Language (XML)", ContentType.Text),
		/** XML  DTD files; Defined by RFC 3023 */
		TYPE_XML_DTD (MIME_TYPE_XML_DTD, "dtd", "XML DTD files; Defined by RFC 3023", ContentType.Application),
		/** SVG+XML (javax.ws.rs.core.MediaType.APPLICATION_SVG_XML) */
		TYPE_SVG_XML (MIME_TYPE_SVG_XML, null, "SVG+XML", ContentType.Application),
		/** HyperText Markup Language (HTML) */
		TYPE_HTML (MIME_TYPE_HTML, "htm;html;shtml", "HyperText Markup Language (HTML)", ContentType.Text),
		/** XHTML */
		TYPE_XHTML (MIME_TYPE_XHTML, "xhtml", "XHTML", ContentType.Text),
		/** Atom feeds */
		TYPE_ATOM_FEEDS_XML (MIME_TYPE_ATOM_FEEDS_XML, null, "Atom feeds", ContentType.Application),
		/** Simple Object Access Protocol (SOAP). Defined by RFC 3902 */
		TYPE_PROTOCOL_SOAP (MIME_TYPE_PROTOCOL_SOAP, null, "Simple Object Access Protocol (SOAP)", ContentType.Application),


		/* Archive formats */
		/** zip archive */
		TYPE_ZIP (MIME_TYPE_ZIP, "zip", "zip archive", ContentType.Application, ContentType.Archive),
		/** GNU Zip-format */
		TYPE_GZIP (MIME_TYPE_GZIP, "gz", "GNU Zip-format", ContentType.Application, ContentType.Archive),
		/** Bzip Archive */
		TYPE_BZIP (MIME_TYPE_BZIP, "bz", "Bzip Archive", ContentType.Application, ContentType.Archive),
		/** Bzip2 Archive */
		TYPE_BZIP2 (MIME_TYPE_BZIP2, "bz2", "Bzip2 Archive", ContentType.Application, ContentType.Archive),
		/** TAR Archive */
		TYPE_TAR (MIME_TYPE_TAR, "tar", "TAR Archive", ContentType.Application, ContentType.Archive),
		/** RAR Archive */
		TYPE_RAR (MIME_TYPE_RAR, "rar", "RAR Archive", ContentType.Application, ContentType.Archive),
		/** 7-zip-archive */
		TYPE_7ZIP (MIME_TYPE_7ZIP, "7z", "7-zip-archive", ContentType.Application, ContentType.Archive),


		/* Model content types */
		/**  WRL files, VRML files; Defined in RFC 2077 */
		TYPE_MODEL_VRML (MIME_TYPE_MODEL_VRML, "vrml", "WRL files, VRML files; Defined in RFC 2077", ContentType.Model),

		/* Message content types */
		/**  Message Http -  Defined in RFC 2616 */
		TYPE_MESSAGE_HTTP (MIME_TYPE_MESSAGE_HTTP, null, "Message Http -  Defined in RFC 2616", ContentType.Message),
		/**  message/partial: Email; Defined in RFC 2045 and RFC 2046 */
		TYPE_MESSAGE_PARTIAL (MIME_TYPE_MESSAGE_PARTIAL, null, "message/partial: Email; Defined in RFC 2045 and RFC 2046", ContentType.Message),
		/**  MESSAGE_RFC822: Email; EML files, MIME files, MHT files, MHTML files; Defined in RFC 2045 and RFC 2046 */
		TYPE_MESSAGE_RFC822 (MIME_TYPE_MESSAGE_RFC822, null, "MESSAGE_RFC822: Email; EML files, MIME files, MHT files, MHTML files; Defined in RFC 2045 and RFC 2046", ContentType.Message),

		/* Multipart content types */
		/** Multipart Form Data */
		TYPE_MULTIPART_FORM_DATA  (MIME_TYPE_MULTIPART_FORM_DATA, null, "Multipart Form Data", ContentType.Multipart),
		/** Multipart Mixed (MIME Email; Defined in RFC 2045 and RFC 2046)*/
		TYPE_MULTIPART_MIXED  (MIME_TYPE_MULTIPART_MIXED, null, "MIME Email; Defined in RFC 2045 and RFC 2046", ContentType.Multipart),
		/** Multipart Signed (Defined in RFC 1847)*/
		TYPE_MULTIPART_SIGNED  (MIME_TYPE_MULTIPART_SIGNED, null, "Multipart Signed - Defined in RFC 1847", ContentType.Multipart),
		/** Multipart Encrypted (Defined in RFC 1847)*/
		TYPE_MULTIPART_ENCRYPTED  (MIME_TYPE_MULTIPART_ENCRYPTED, null, "Multipart Encrypted - Defined in RFC 1847", ContentType.Multipart),

		/* Other "Prefix X" types */
		/**  Form Encoded Data; Documented in HTML 4.01 Specification, Section 17.13.4.1 */
		TYPE_WWW_FORM_URLENCODED (MIME_TYPE_WWW_FORM_URLENCODED, null, " Form Encoded Data; Documented in HTML 4.01 Specification, Section 17.13.4.1", ContentType.Application)

		;


		/** The mime type. */
		private String mimeType;
		private String description;

		private String[] fileTypes;
		private ContentType[] contentTypes;


		MimeType(String mimeType, String fileType, String description, ContentType... contentTypes) {
			this.mimeType = mimeType;
			this.fileTypes = splitTypes(fileType);
			this.description = description;
			this.contentTypes = contentTypes;
		}



		private final String[] splitTypes(String fileType) {
			return fileType != null ? fileType.split(";") : new String[] {};
		};


		/**
		 * @return the mimeType
		 */
		public String getMimeType() {
			return mimeType;
		}


		/**
		 * @return the description
		 */
		public String getDescription() {
			return description;
		}


		/**
		 * @return the categories
		 */
		public ContentType[] getContentTypes() {
			return contentTypes;
		}


		/**
		 * @return the fileTypes
		 */
		public String[] getFileTypes() {
			return fileTypes;
		}


		public static enum ContentType {
			/* Text - can be used to represent textual information in a number of character sets and formatted text description languages in a standardized manner. */
			Text,
			/* Audio - for transmitting audio or voice data.*/
			Audio,
			/* Image - transmitting still image (picture) data.*/
			Image,
			/* Video -  for transmitting video or moving image data, possibly with audio as part of the composite video data format.*/
			Video,
			/* Application -  can be used to transmit application data or binary data.*/
			Application,
			/* Model - to describe model data (For 3D models.) */
			Model,
			/* Message - for encapsulating a mail message. */
			Message,
			/* Multipart - can be used to combine several body parts, possibly of differing types of data, into a single message.*/
			Multipart,

			//!!extra, non RFC 2046 conform types//

			/* Any documents representing textual & other info */
			Document,
			/* Binary compressed file containters */
			Archive
		}

	}
}
