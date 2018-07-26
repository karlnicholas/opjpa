<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="aboutForm">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
</head>
<body>
  Comments from: <p><xsl:value-of select="email"/></p>
  <p><span><xsl:value-of select="comment"/></span></p>
</body>
</xsl:template>
</xsl:stylesheet>