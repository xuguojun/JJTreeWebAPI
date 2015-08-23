<%-- 
    Document   : Block
    Created on : Jul 26, 2015, 12:59:27 AM
    Author     : rose
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
        
        <link rel="stylesheet" href="styles/<%= request.getParameter("style") %>">
        <script src="highlight.pack.js"></script>
        <script>hljs.initHighlightingOnLoad();</script>
        <style>
            body { margin: 0; padding: 0; }
        </style>
    </head>
    <body>
          
        <pre><code class="lang-objectivec">

#import "KTBaseParser.h"

@interface KTChartListParser : KTBaseParser

+ (NSArray *)parseDistribution:(NSDictionary *)dictionary;

// special
+ (NSArray *)parseTiming:(NSDictionary *)dictionary;
+ (NSArray *)parseTimingDistribution:(NSDictionary *)dictionary;

@end

#import "KTBaseParser.h"

@interface KTChartListParser : KTBaseParser

+ (NSArray *)parseDistribution:(NSDictionary *)dictionary;

// special
+ (NSArray *)parseTiming:(NSDictionary *)dictionary;
+ (NSArray *)parseTimingDistribution:(NSDictionary *)dictionary;

@end

        </code></pre>
        
    </body>
</html>
