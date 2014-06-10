<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:dwc="http://rs.tdwg.org/dwc/terms/" xmlns:dwcc="http://rs.tdwg.org/dwc/curatorial/"
    xmlns:dc="http://purl.org/dc/terms/" xmlns:geo="http://www.w3.org/2003/01/geo/wgs84_pos#"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    exclude-result-prefixes="xs rdf geo dc dwcc dwc" version="2.0">

    <xsl:param name="selectedNum"/>

    <xsl:variable name="doc1" select="document('B100146595.rdf')"/>
    <xsl:variable name="doc2" select="document('B200049849.rdf')"/>
    <xsl:variable name="doc3" select="document('BW06960010.rdf')"/>
    <xsl:variable name="doc4" select="document('BW17980020.rdf')"/>

    <xsl:output method="html" indent="yes"/>

    <xsl:template match="/">
        <xsl:text disable-output-escaping="yes">&lt;!DOCTYPE html></xsl:text>
        <html>
            <head>
                <meta charset="utf-8"/>
                <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
                <meta name="viewport" content="width=device-width, initial-scale=1"/>
                <link href="css/bootstrap.min.css" rel="stylesheet"/>
                <link href="css/sticky-footer.css" rel="stylesheet"/>
                <!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
                <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js"/>
                <!-- Include all compiled plugins (below), or include individual files as needed -->
                <script src="js/bootstrap.min.js"/>
            </head>
            <body role="document">

                <!-- Fixed navbar -->
                <div class="navbar navbar-inverse navbar-fixed-top" role="navigation">
                    <div class="container">
                        <div class="navbar-header">
                            <button type="button" class="navbar-toggle" data-toggle="collapse"
                                data-target=".navbar-collapse">
                                <span class="sr-only">Toggle navigation</span>
                                <span class="icon-bar"/>
                                <span class="icon-bar"/>
                                <span class="icon-bar"/>
                            </button>
                            <a class="navbar-brand" href="#">Farnquiz</a>
                        </div>
                        <div class="navbar-collapse collapse">

                        </div>
                        <!--/.nav-collapse -->
                    </div>
                </div>

                <div class="container theme-showcase" role="main">

                    <!-- Main jumbotron for a primary marketing message or call to action -->
                    <div class="jumbotron">
                        <h1>Rate den Pflanzennamen!</h1>
                        <p>Benutzt Daten des Botanischen Museums.</p>
                    </div>
                    
                    <div class="page-header">
                        <h1>Bild</h1>
                    </div>
                    <div class="row">
                        <div class="col-sm-4" style="width:30%; height: 30%;">
                            <xsl:choose>
                                <xsl:when test="$selectedNum='1'">
                                    <xsl:apply-templates select="$doc1/rdf:RDF/rdf:Description/dwc:associatedMedia"
                                    />
                                </xsl:when>
                                <xsl:when test="$selectedNum='2'">
                                    <xsl:apply-templates select="$doc2/rdf:RDF/rdf:Description/dwc:associatedMedia"
                                    />
                                </xsl:when>
                                <xsl:when test="$selectedNum='3'">
                                    <xsl:apply-templates select="$doc3/rdf:RDF/rdf:Description/dwc:associatedMedia"
                                    />
                                </xsl:when>
                                <xsl:when test="$selectedNum='4'">
                                    <xsl:apply-templates select="$doc4/rdf:RDF/rdf:Description/dwc:associatedMedia"
                                    />
                                </xsl:when>
                            </xsl:choose>
                        </div>
                        <div class="col-sm-8">
                            <p>
                                <xsl:apply-templates select="$doc1/rdf:RDF">
                                    <xsl:with-param name="number">1</xsl:with-param>
                                </xsl:apply-templates>
                                <xsl:apply-templates select="$doc2/rdf:RDF">
                                    <xsl:with-param name="number">2</xsl:with-param>
                                </xsl:apply-templates>
                                <xsl:apply-templates select="$doc3/rdf:RDF">
                                    <xsl:with-param name="number">3</xsl:with-param>
                                </xsl:apply-templates>
                                <xsl:apply-templates select="$doc4/rdf:RDF">
                                    <xsl:with-param name="number">4</xsl:with-param>
                                </xsl:apply-templates>
                            </p>
                        </div>
                    </div>
                </div>
                <!-- /container -->
                <div id="footer">
                    <div class="container">
                        <p class="text-muted">© 2014 Farnquiz</p>
                    </div>
                </div>
                <!-- Modal -->
                <div class="modal fade" id="successModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
                    <div class="modal-dialog">
                        <div class="modal-content">
                            <div class="modal-header">
                                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">X</button>
                                <h4 class="modal-title" id="myModalLabel">Antwort</h4>
                            </div>
                            <div class="modal-body">
                                Richtig
                            </div>
                            <div class="modal-footer">
                                <button type="button" class="btn btn-default" data-dismiss="modal">Schließen</button>
                                <a href=""><button type="button" class="btn btn-primary">Nächste Pflanze</button></a>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="modal fade" id="failModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
                    <div class="modal-dialog">
                        <div class="modal-content">
                            <div class="modal-header">
                                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">X</button>
                                <h4 class="modal-title" id="myModalLabel">Antwort</h4>
                            </div>
                            <div class="modal-body">
                                Falsch
                            </div>
                            <div class="modal-footer">
                                <button type="button" class="btn btn-default" data-dismiss="modal">Schließen</button>
                                <a href=""><button type="button" class="btn btn-primary">Nächste Pflanze</button></a>
                            </div>
                        </div>
                    </div>
                </div>
            </body>
        </html>
    </xsl:template>

    <xsl:template match="rdf:RDF">
        <xsl:param name="number"/>
        <xsl:apply-templates select="rdf:Description">
            <xsl:with-param name="number" select="$number"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="rdf:Description">
        <xsl:param name="number"/>

        <xsl:apply-templates select="dc:title">
            <xsl:with-param name="number" select="$number"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="dc:title">
        <xsl:param name="number"/>
        <button type="button" class="btn btn-lg btn-default" style="min-width: 425px; margin-bottom: 25px;">
            <xsl:if test="$number = $selectedNum">
                <xsl:attribute name="data-toggle">modal</xsl:attribute>
                <xsl:attribute name="data-target">#successModal</xsl:attribute>
            </xsl:if>
            <xsl:if test="$number != $selectedNum">
                <xsl:attribute name="data-toggle">modal</xsl:attribute>
                <xsl:attribute name="data-target">#failModal</xsl:attribute>
            </xsl:if>
            <xsl:value-of select="."/>
        </button><br/>
    </xsl:template>

    <xsl:template match="dwc:associatedMedia">
        <img class="img-thumbnail">
            <xsl:attribute name="src" select="."/>
        </img>
    </xsl:template>

    <xsl:template match="*|node()"/>
</xsl:stylesheet>
