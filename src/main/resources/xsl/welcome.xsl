<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="user">
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
</head>
<body>
  <p>Dear <xsl:value-of select="firstName" /> <xsl:value-of select="lastName" /></p>
  <p>Welcome to the <a href="http://op-opca.b9ad.pro-us-east-1.openshiftapps.com">Court Opinions</a> application.</p>
  <p>You have been registered with the email <xsl:value-of select="email" />.</p>
  <p>This website is dedicated to serving attorneys in California. In addition to other features, this website analyzes newly published California Court opinions (Slip Opinions), showing what statutes and cases are important. Please take a moment to have a look.</p>
  <p>If you wish to opt out of all further communications, please click the link below.</p>
  <p><xsl:element name="a">
    <xsl:attribute name="href">http://op-opca.b9ad.pro-us-east-1.openshiftapps.com/optout?email=<xsl:value-of select="email"/>&quot;key=<xsl:value-of select="optoutKey"/></xsl:attribute>
    <span>Opt out of Court Opinions</span>
    </xsl:element></p>
  <p>We hope you will appreciate this effort.</p>
  <p>With Kind Regards, <br /><br />Court Opinions.
  </p>
</body>
</html>
</xsl:template>
</xsl:stylesheet>