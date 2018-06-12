# comdagen

Comdagen (_Com_merce _Da_ta _Gen_erator) is a tool that will build you a site import file tailored to your specification.

Comdagen is developed primarly by Matti Bickel (<mbickel@salesforce.com>). Version 1.0 was written almost entirely by Oskar Jauch (@ojauch).
Initial ideas were brought by Rene Schwietzke (@rschwietzke). Martin Klaus and Matti Bickel contributed to earlier versions.

_Note_: historically, this project has been known as undagen (Unified Data Generator). But we want to very specific about the type 
of data this project is able to produce - you'll get a full fledged Commerce Cloud data set. Nothing more, nothing less.  

## Build

This tool uses Maven to build. From a command line, switch to your checkout directory and issue the `package` command:

```
    $ mvn package
```

The executable JAR file will be placed in `target/comdagen-<VERSION>-SNAPSHOT.jar`.

## Usage

Execute comdagen by calling one of:

```
    $ java -jar target/comdagen-1.1-SNAPSHOT.jar --zip
    $ ~/comdagen/bin/comdagen.sh --zip
```

This will automatically generate a zip file containing site data in the `output` folder.

You can tweak how comdagen runs in two ways:

### Command line parameters

```
  --config               : Use this config file to specify which sites to generate content for 
                           (default: $configDir/sites.yaml)
  --configDir            : Generate xml files for all configs in this dir (default: ./config)
  --output               : Generate xml files in this directory (default: ./output)
  --zip-output           : Where to put the final site import zip file (default: ./output/generated.zip)
  --zip                  : Whether to create a zip file at all (default: false)
```

If you give a `configDir` option but no `config`, the directory must contain a `sites.yaml`.
All other configuration files will be read from the `sites.yaml` file (see the next section).

### Config files

The main config file is `sites.yaml`. This source comes with a default one in the `config` directory. Please see it's
comments for more information.

By removing the respective `Config` entries from the site entry in `sites.yaml` you can avoid generating data you don't
need. For example:

```
sitesConfig:
  sites:
    - regions: [Generic, German, Chinese]
      currencies: [USD, EUR, CNY]
      outputFilePattern: "site.xml"
      outputDir: "sites"
      customerConfig: "customers.yaml"
      pricebookConfig: "pricebooks.yaml"
      catalogConfig: "catalogs.yaml"
```
Will generate a customers, pricebooks and corresponding catalog.

On the other hand, if you are after a specific element, consider:
```
sitesConfig:
  sites:
    - regions: [Generic, German, Chinese]
      currencies: [USD, EUR, CNY]
      outputFilePattern: "site.xml"
      outputDir: "sites"
      customerConfig: "customers.yaml"
#      pricebookConfig: "pricebooks.yaml"
#      catalogConfig: "catalogs.yaml"
```
Commenting out the config files will result in only a site and customer xml file to be generated. You can find the
output in the `output` directory.

Open and edit the respective config files to adjust the generation result. Two entries are present everywhere:

 - the `initialSeed` attribute controls the generated content - comdagen will generate the same content as long as the 
   seed stays the same

 - the `elementCount` attribute controls how many elements get generated. Note that the first X elements will be the 
   same if you just increase this value without changing `initialSeed`.

### Data sources
Comdagen uses data files to pick realistic names, cities or zip codes for the generated data sets. You can find those
files in your checkout under `src/main/resources/contentfiles`. They are not meant to provide accurate data, the main
goal is to get character and word distribution correct for our internal localized indexers.

We used data from the following web-sites to compile our files:
* German:
  - https://en.wiktionary.org/wiki/Appendix:German_surnames
  - https://en.wiktionary.org/wiki/Appendix:German_given_names
  - https://www.namen-liste.de/strassen-a/ (automated compilation of list of names)
  - Zip codes and cities based on the [OpenGeoDB dataset](http://www.fa-technik.adfc.de/code/opengeodb/)
* Chinese:
  - http://technology.chtsai.org/namelist/
  - https://motivationmentalist.com/2014/06/14/hundred-family-surnames-bai-jia-xing-with-pinyin/
  - https://zh.wikipedia.org/wiki/中华人民共和国境内地区邮政编码列表 (for zips and city information)
* Russian: 
  - https://web.archive.org/web/20040202083132/http://allfamilii.narod.ru/12.htm (names)
  - https://en.wiktionary.org/w/index.php?title=Category:Russian_given_names

For the "generic" locale, we use randomly generated words/sentences.

Wiktionary sites are subject to [CC-BY-SA](https://creativecommons.org/licenses/by-sa/3.0/). [Chinese name list](http://technology.chtsai.org/namelist/) 
by Chih-Hao Tsai, used with permission by the author.

For longer text excerpts we use snippets from copyright-free books downloaded from [Project Gutenberg](http://www.gutenberg.org). 
The headers are left intact, so you can see which books are used. 
