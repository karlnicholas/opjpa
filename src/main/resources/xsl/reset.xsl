<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="user">
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
</head>
<body>
  <p>Hello, <xsl:value-of select="email" /></p>
  <p>Please click on the following link to verify your email address. If you did not request a reset, please ignore this email or contact Court Opinions through our website.</p>
  <h4><xsl:element name="a">
    <xsl:attribute name="href">http://op-cacode.rhcloud.com/reset?email=<xsl:value-of select="email"/>&quot;key=<xsl:value-of select="verifyKey"/></xsl:attribute>
    <span>Reset Email</span>
    </xsl:element>
  </h4>
  <p>
    Regards, <br /><br />
    Court Opinions.
  </p>
</body>
</html>
</xsl:template>
</xsl:stylesheet>