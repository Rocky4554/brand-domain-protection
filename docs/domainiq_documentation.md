API Documentation
Introduction
domainIQ offers an API interface that can be used to access commonly-used tools and services available on the site. The API can be accessed by a wide range of scripts using simple HTTP GET and POST requests.

Basics
Use of the API requires a current, valid domainIQ account. All paid account types include API access. The features available in the API are the same as the ones offered through the site, and the API uses the same account-based limits. In other words, queries performed using the API to access services that have daily, monthly or count-based limits will be the asme as their web equivalents.

Interaction with the API is performed using HTTP (web) requests. Parameters to the API may be sent via either GET or POST; note, however, that larger requests should be sent using POST.

Working with the API
The API endpoint is https://www.domainiq.com/api. All requests to the API will use this URL.

Authentication to the API is performed using a 32-character key, which is available under the Tools page. Click here to view or change your key.

Keep your key safe and treat it as you would a password. Remember that while you can change your key at any time, doing so will immediately deactivate the previous one.

Sending requests to the API
API requests consist of at least two parameters: the API key, the service name, and, in almost all cases, one or more service parameters.

An example API request might look something like this:

https://www.domainiq.com/api?key=sample&service=domain_report&domain=google.com
Required parameters
At a minimum, the key and service parameters are required.

Processing results from the API
Results from the API are returned in XML format. Some services can provide other output modes, such as CSV or text. Available output modes for each service are specified below. If the specified mode is unavailable for the service, the default XML format will be used.

Specifying output mode
Pass the output_mode parameter to the API to specify the mode of output for the results. Valid output modes include:

xml: standard XML output
json: standard JSON output
csv: CSV output; fields enclosed by " and separated by ,.
ini: INI file format output. Any tool that can produce output in CSV format can also use the INI format. Note that INI records will include an extra record_index property for each row (starting at 1).
Using the API in a web browser
The API is intended primarily for automated use, but it can also be used in a web browser.

For JSON output modes, we recommend using a JSON formatting plugin such as this one for Chrome. Most browsers will automatically format XML for readability.

If you wish to download an API request as a file, pass the parameter as_file=1 with your request. For example:

https://www.domainiq.com/api?key=sample&service=whois&domain=google.com&output_mode=xml&as_file=1
will result in a file download titled API.whois.NNNNN.xml (where NNNNN is a timestamp number).

Normal versus Queued requests
Some API calls may take longer than a few seconds to execute, depending on the amount and complexity of the data involved. For API calls that are expected to take time, a feature called "queued mode" exists. When you specify queued=1, the API will instantly return a session token that can be used in subsequent calls to check on the status of a queued call or retrieve its output.

Most API calls can be made in queued mode. See the section below for examples of how to check status and retrieve results.

Note: Results from the queue service will always default to XML format if CSV or INI format is used, except when calling the results action, which will return the results of your original API call in the original format you specified.

Working with Queued Requests
To start a queued request, use a normal API call and add the queued=1 parameter.

https://www.domainiq.com/api?key=sample&service=domain_report&domain=google.com&queued=1
Sample output:

<`?`xml version="1.0" encoding="UTF-8"?>
<queue>
    <queued>1</queued>
    <hash>g1aqpj6c7qdcoe4yiqi9f359vzgu73uu</hash>
</queue>
After starting a queued request, you can check on its status:

https://www.domainiq.com/api?key=sample&service=queue&hash=g1aqpj6c7qdcoe4yiqi9f359vzgu73uu&action=status
Sample output:

<`?`xml version="1.0" encoding="UTF-8"?>
<queue>
    <status>Complete</status>
    <output_mode>xml</output_mode>
    <last_update>2016-12-29 14:55:52</last_update>
    <result_size>231269</result_size>
</queue>
Once the status call returns Complete, fetch the results of the call:

https://www.domainiq.com/api?key=sample&service=queue&hash=g1aqpj6c7qdcoe4yiqi9f359vzgu73uu&action=results
The contents of the API call will be returned as normal.

Available Services
Domain, email, name and IP summary reports
Basic summary reports for individual domain names, email addresses, registrant names and IP addresses can be retrieved with the domain_report, email_report, name_report and ip_report services.

These reports return a variety of fields. Most are fairly self-explanatory in nature.

Domain reports
Call the domain_report service. Specify the domain name via the domain parameter.

https://www.domainiq.com/api?key=sample&service=domain_report&domain=google.com
If your account has been configured to allow a higher usage limit for cached-only reports, you can enable cached mode by adding the cached=1 parameter to the request. This will return cached results for the domain if available.

https://www.domainiq.com/api?key=sample&service=domain_report&domain=google.com&cached=1
Registrant name reports
Call the name_report service. Specify the registrant's name via the name parameter.

https://www.domainiq.com/api?key=sample&service=name_report&name=Jeph+Smythe
Registrant organization reports
Call the organization_report service. Specify the registrant's name via the organization parameter.

https://www.domainiq.com/api?key=sample&service=organization_report&organization=My+Company
Registrant email reports
Call the email_report service. Specify the email address via the email parameter.

https://www.domainiq.com/api?key=sample&service=email_report&email=someone@somewhere.com
IP address summary reports
Call the ip_report service. Specify the IP address name via the ip parameter.

https://www.domainiq.com/api?key=sample&service=ip_report&ip=1.2.3.4
Reverse lookup services
The tools in this category allow you to retrieve information in bulk for domain names, IP addresses, MX records and more. They provide functionality equivalent to the services on the Tools page.

Domain keyword search
Perform a search for domain names matching one or more keywords. Returns domain names with summarized registrant information. All output modes supported; defaults to CSV.

Keyword parameters:

keyword - specify one or more keywords. To specify multiple keywords, append a number in brackets to the parameter, e.g. keyword[0], keyword[1], etc.
condition - specify the type of keyword match to use. Available conditions include:
contains - domain must contain the keyword. This is the default if condition is omitted.
starts - domain must begin with the keyword.
ends - domain (SLD portion) must end with keyword.
does_not_contain - domain must not contain keyword.
does_not_start_with - domain must not begin with keyword.
does_not_end_with - domain (SLD portion) must not end with keyword.
match - when multiple keywords are specified, you can control whether all of them must match, or just one of them, by passing any or all. Default is any.
Result control parameters:

count_only - if included, the API call will just return a count of all matched domain names without details. You can re-run the call without the parameter to fetch the full results, which will be cached automatically.
exclude_dashed - set to 1 to exclude domains with dashes (-).
exclude_numbers - set to 1 to exclude domains with numbers (0 through 9).
exclude_idn - set to 1 to exclude IDN domains (domains that begin with xn--).
min_length - numeric; the minimum permitted match length (excluding TLD).
max_length - numeric; the maximum permitted match length (excluding TLD).
min_create_date and max_create_date - string; filter the results by minimum and/or maximum WHOIS creation date. Dates should be in YYYY-MM-DD format.
limit - limit the number of results. Defaults to the maximum number of results available for your account.
If specifying multiple keywords, you can specify multiple conditions by matching the number appended to keyword. For example, condition[1] corresponds to keyword[1].

This tool is rate-limited. Users are allowed a maximum of three concurrent domain search API calls. See below for an example of a rate-limited output.

Simple example: find all domains containing "glengarry":

https://www.domainiq.com/api?key=sample&service=domain_search&keyword=glengarry
Retrieving just the count of found domains for the above search:

https://www.domainiq.com/api?key=sample&service=domain_search&keyword=glengarry&count_only=1
More advanced example: find domains that either begin with "baseball" or contain "football".

https://www.domainiq.com/api?key=sample&service=domain_search&keyword[0]=baseball&condition[0]=starts&keyword[1]=basketball&condition[1]=contains&match=any
Another advanced example: find domains that begin with "first" and end with "bank".

https://www.domainiq.com/api?key=sample&service=domain_search&keyword[0]=first&condition[0]=starts&keyword[1]=bank&condition[1]=ends&match=all
Example output:

domain	tld	sld_length	whois_registrant	whois_emails	whois_age	dns
glengarryouthouses.com	com	19	Julia Lucio	jmlucio@versacore.info	1	NS671.VERSACOREHOSTING.COM
glengarryheritagecentre.com	com	24	Robert MacCallum	domains@maccallum.co.uk	7	NS1.TURBODNS.CO.UK
glengarrygroupconsulting.com	com	25	Mackintosh, Scott	smackintosh@sympatico.ca	6	NS1.SHIFTIDEAS.COM
glengarryfinecheese.com	com	20	Margaret Morris	domain@360elements.ca,domain@360elements.com	5	NS1.JTEC.CA
98glengarry.com	com	12	Jolene Van Dyk	tech@point2homes.biz,jolenevandyk@deerbrookrealty.com	0	NS1.POINT2.COM
glengarrybrewing.com	com	17	David Hladky	dhladky@gmail.com	0	NS1.APLUS.NET
glengarryhighlands.net	net	19	Jason Martinez	jason@thestarrconspiracy.com	1	NS47.DOMAINCONTROL.COM
Example count-only output:

<`?`xml version="1.0" encoding="UTF-8"?>
<count>196</count>
If too many other concurrent domain search API calls are active for your account, you will receive the following error:

<`?`xml version="1.0" encoding="UTF-8"?>
<error>
    <error>Too many other concurrent domain searches. Please wait for the other searches to complete.</error>
    <concurrent_error>1</concurrent_error>
    <concurrent_sessions>3</concurrent_sessions>
</error>
Registrant, email address and organization wildcard search
Perform a search for registrant names, email addresses, or organization names using a wildcard match. Returns a list of names or email addresses with the total number of domain names associates with each entity. XML and JSON output supported.

This advanced service is only available for certain account types. Please contact us if you wish to enable it for your account.

Parameters:

type - the search type. Options include email, name or org.
search - the search phrase. Remove all spaces and other punctuation for names; email addresses may contain alphanumeric characters and '-', '_', . and @.
match - the match type. Defaults to contains; other options are begins or ends.
The service will return up to 100 results, sorted by number of associated domain names, from a maximum result set of 10,000. If your search criteria is too broad, a message will appear indicating that the total result set is too large for sorting and your criteria may want to be refined as a result.

Note: the match type will affect the speed of the search. begins matches are generally much faster than contains or ends matches, which may take a minute or more to complete processing.

Registrant name example: find all registrant names containing "Gary Smith".

https://www.domainiq.com/api?key=sample&service=reverse_search&type=name&search=garysmith
Email example: find all email addresses at the @microsoft.com domain name.

https://www.domainiq.com/api?key=sample&service=reverse_search&type=email&search=@microsoft.com&match=ends
Example output:

<`?`xml version="1.0" encoding="UTF-8"?>
<results>
    <message>27 total results found</message>
    <items>
        <item>
            <item>GARY SMITH</item>
            <domain_count>3196</domain_count>
        </item>
        <item>
            <item>GARY SMITH JR</item>
            <domain_count>72</domain_count>
        </item>
        ...
    </items>
</results>
Reverse DNS search
Find all domains hosted on a DNS (domain nameserver) hostname. Returns domain names, whois registrant and emails, associated counts for registrants and emails, and domain registrars. All output modes supported; defaults to CSV.

Parameters:

domain - the domain name of the DNS host to look up. Can be a complete DNS hostname (e.g. ns1.google.com) or just a domain name (e.g. yahoo.com).
Example: find all domains hosted on the DNS host science.com.

https://www.domainiq.com/api?key=sample&service=reverse_dns&domain=science.com
Sample output:

domain	ip	whois_registrant	whois_registrant_domain_count	whois_email	whois_email_domain_count	whois_registrar
pchbingo.org	164.109.20.13	publishers clearing house	753	domainadmin@pchmail.com	275	Network Solutions, LLC
educatedape.com	54.221.211.154	christopher joseph	51	contact@chrisjoseph.com	16	MISK.COM, INC.
todayshealthjournal.com	68.64.49.76	private registrant	30205611	97d59c.protect@whoisguard.com	1	ENOM, INC.
lawdrill.net	69.25.5.93	greenberg traurig	278	chidomainmanagement@gtlaw.com	403	NETWORK SOLUTIONS, LLC.
radiostream123.com	87.117.217.45	private registrant	30205611	radiostream123.com@domainsbyproxy.com	1	GODADDY.COM, LLC
gaggleamp.biz	50.63.202.14	glenn gaudet	10	glenn@gaggleamp.com	7	: GODADDY.COM, INC.
thestreetratings.org	107.23.91.192	thestreet.com, inc.	250	internic-admin@thestreet.com	92	Network Solutions, LLC
kayanetticaret.com	46.165.204.241	ali mert kaya	1	mrtkaya_2@hotmail.com	1	IHS TELEKOM, INC.
ruudpaybills.com	63.76.193.153	rheem webmaster	23	webmaster@rheem.com	45	DNC Holdings, Inc.
ordershredz.com	184.168.221.3	arvin lal	34	admin@getshredz.com	102	GODADDY.COM, LLC
Reverse IP search
Find all domains on an IP address, subnet, block, IP range or domain name (which will be resolved to an IP address). Returns domain names, whois registrant and emails, associated counts for registrants and emails, and domain registrars. Defaults to CSV output, but all output modes are supported.

Parameters:

type - possible values include:
ip - find domains on an IP address.
subnet - find domains on a subnet (e.g. 1.1.1.1 - 1.1.1.255).
block - find domains on a block (e.g. 1.1.1.1 - 1.1.255.255).
range - find domains with IP addresses in the specified range. Separate IP addresses with -.
domain - find domains on a domain's IP address. The domain will be resolved to an IP address automatically.
data - an IP address (e.g. 12.34.56.78), subnet (e.g. 12.34.45), block, (e.g. 12.34), range (e.g. 12.34.56.78-12.34.56.90), or domain name (will be resolved to IP).
IP example: find all domains on the IP address 4.3.2.1.

https://www.domainiq.com/api?key=sample&service=reverse_ip&type=ip&data=4.3.2.1
Range example: find all domains between IP addresses 4.3.2.1 and 4.3.2.21.

https://www.domainiq.com/api?key=sample&service=reverse_ip&type=range&data=4.3.2.1-4.3.2.21
Subnet example: find all domains on subnet 4.3.2.0 (all IPs between 4.3.2.1 and 4.3.2.254).

https://www.domainiq.com/api?key=sample&service=reverse_ip&type=subnet&data=4.3.2.0
Domain example: find all domains on domain sampledomain.com's IP address.

https://www.domainiq.com/api?key=sample&service=reverse_ip&type=domain&data=sampledomain.com
Sample output:

domain	ip	whois_registrant	whois_registrant_domain_count	whois_email	whois_email_domain_count	whois_registrar	create_date
townofgravity.com	4.3.2.1	joe grinnell	3	dns@grinnell.net	2	GODADDY.COM, LLC
legendarygraphics.com	4.3.2.1	private registrant	30205611	proxy145947@1and1-private-registration.com	1	1 & 1 Internet AG	2012-01-02
telephant.org	61.114.155.50	limited liability company "data express"	3	tolik@dataxp.net	2	Regional Network Information Center, JSC dba	2014-04-06
misterpinganillos.com	87.106.194.92	juan antonio ibanez santorum	8	juanito1982@gmail.com	8	1 & 1 Internet AG	2009-12-21
crowdcode.net	4.3.2.1	private registrant	30205611	crowdcode.net+t@privatedomainaccounts.com	1	BADGER INC	2013-04-04
Reverse MX search
Find all domains using a particular mail server by IP address, IP address range or mail server hostname. Returns domain names, MX server IP address and hostname, whois registrant and emails, associated counts for registrants and emails, and domain registrars. Defaults to CSV output, but all output modes are supported.

Parameters:

type - possible values include:
hostname - find domains using the specified MX hostname.
ip - find domains using a MX server on an IP address.
subnet - find domains using a MX server on a subnet (e.g. 1.1.1.1 - 1.1.1.255).
block - find domains using a MX server on a block (e.g. 1.1.1.1 - 1.1.255.255).
range - find domains using a MX server with IP addresses in the specified range. Separate IP addresses with -.
data - an IP address (e.g. 12.34.56.78), subnet (e.g. 12.34.45), block, (e.g. 12.34), range (e.g. 12.34.56.78-12.34.56.90), or domain name (will be resolved to IP).
recursive - if specified, recursively check MX hostnames to discover more domains.
Hostname example: find all domains on the mail server hostname msgin.vvv.facebook.com.

http://domainiq.com/api?key=sample&service=reverse_mx&type=hostname&data=msgin.vvv.facebook.com
Range example: find all domains on MX servers with IP addresses between 4.3.2.1 and 4.3.2.21.

https://www.domainiq.com/api?key=sample&service=reverse_mx&type=range&data=4.3.2.1-4.3.2.21
Sample output:

domain	mx_ip	mx_hostname	whois_registrant	whois_registrant_domain_count	whois_email	whois_email_domain_count	whois_registrar	create_date
facebook.com	66.220.155.14	msgin.t.facebook.com.	facebook, inc.	1067	domain@fb.com	1081	MARKMONITOR INC.	2001-01-02
facebookappmail.com	66.220.155.15	msgin.t.facebook.com.	facebook, inc.	1067	domain@fb.com	1081	MARKMONITOR INC.	2004-01-01
facebookmx.com	69.171.244.12	msgin.t.facebook.com.	facebook, inc.	1067	domain@fb.com	1081	MARKMONITOR INC.	2009-01-02
facebookmail.com	69.171.244.12	msgin.t.facebook.com.	facebook, inc.	1067	domain@fb.com	1081	MARKMONITOR INC.	2012-01-02
Bulk lookup services
Note that bulk tools often involve sending and receiving large amounts of data, and some bulk tools may take a long time to complete depending on the amount of data being processed. You may want to use POST requests to send input data. If you expect a request to take a long time to process, you may want to use the queued request mode (described above in the Normal versus Queued requests section above).

Bulk DNS lookup
The bulk DNS lookup tool can be used to look up the DNS hosts for many domains at once. It is equivalent to the Bulk DNS tool. CSV output only.

Most accounts are limited to a certain number of bulk DNS lookups per day. Refer to your account's Limits page or the output of the limits service to see how many remain.

Parameters:

domains - one or more domain names. Separate multiple domain names with >>.
type - one or more DNS record types. Separate multiple types with comma (,). Possible types include:
A - A records (IPv4 addresses).
AAAA - AAAA records (IPv6 addresses).
CNAME - CNAME (canonical name) records.
MX - MX (mail server) records.
NS - NS (nameserver) records.
TXT - TXT records.
SOA - SOA (Start of Authority) records.
If omitted, type defaults to NS.

Example: look up the DNS hosts for a list of domains.

https://www.domainiq.com/api?key=sample&service=bulk_dns&domains=red.com>>green.com>>blue.com>>black.com>>white.com>>yellow.com>>orange.com
Sample output:

domain	dns_1	dns_2
orange.com	l4.nstld.com	a4.nstld.com
red.com	ns2.p09.dynect.net	ns1.p09.dynect.net
black.com	ns2.voxel.net	ns3.voxel.net
yellow.com	ns-756.awsdns-30.net	ns-1575.awsdns-04.co.uk
blue.com	ns2.digitalocean.com	ns1.digitalocean.com
white.com	ns1.yours.pl	ns2.yours.pl
Bulk WHOIS lookup
The bulk WHOIS lookup tool allows you to retrieve WHOIS data for a number of domains or IP addresses at once. Data is returned in CSV format.

This tool is limited to a certain number of lookups per day. Refer to your account's Limits page or the output of the limits service to see how many remain. Additional daily credits can be purchased and we offer a variety of standard or custom upgrade options to increase the limit -- contact us for more details.

Parameters:

type - the type of lookup to perform. Possible options:
live - live WHOIS lookup. The slowest of the three options; also provides the most up-to-date data. Default.
registry - live registry-only WHOIS lookup. Does not contain contact information or other details. Only affects COM, NET and other domains that have WHOIS records consisting of both registry- and registrar-level data.
cached - cached WHOIS lookup. Much faster than live or registry; contains full data but may be out of date by up to 30 days.
domains or ips - one or more domain names or IP addresses (IPv4 or IPv6). Because of differences in fields, a batch should contain only domain names or IP addresses, not both.
Example: look up WHOIS data for a set of domain names.

https://www.domainiq.com/api?key=sample&service=bulk_whois&domains=red.com>>green.com>>blue.com>>white.com>>orange.com>>black.com>>yellow.com
The tool generates a report comprising all data in the WHOIS record (typically 47 columns total). Abbreviated sample output:

domain	status	registrar	creation_date	expiration_date	update_date
blue.com	CLIENTTRANSFERPROHIBITED	DOMAIN.COM, LLC	11/7/1996	10/29/2019	2/20/2015
red.com	CLIENTDELETEPROHIBITED	DNC HOLDINGS, INC.	10/27/1992	10/26/2021	11/20/2013
green.com	CLIENTDELETEPROHIBITED	MARKMONITOR INC.	2/10/1999	5/20/2015	4/18/2014
black.com	CLIENTTRANSFERPROHIBITED	NETWORK SOLUTIONS, LLC.	9/25/1994	9/24/2023	11/26/2014
yellow.com	CLIENTTRANSFERPROHIBITED	ENOM, INC.	8/19/1994	8/18/2015	10/21/2014
white.com	CLIENTTRANSFERPROHIBITED	FABULOUS.COM PTY LTD.	11/17/1996	11/16/2019	11/3/2014
orange.com	CLIENTTRANSFERPROHIBITED	CSC CORPORATE DOMAINS, INC.	12/9/1993	12/8/2015	12/4/2014
Bulk domain IP WHOIS lookup
This tool resolves domain names to IP addresses and checks ownership information and geolocation for the IPs. The API equivalent of the Bulk WHOIS IP Research tool.

Note that this tool is different than the Bulk WHOIS tool above -- if you want to look up WHOIS data for a set of IP addresses, use Bulk WHOIS instead.

This tool is limited to a certain number of lookups per day. Refer to your account's Limits page or the output of the limits service to see how many remain.

Parameters:

domains - one or more domain names. Separate multiple domain names with >>.
Example: look up IP WHOIS information for a set of domain names.

https://www.domainiq.com/api?key=sample&service=bulk_whois_ip&domains=red.com>>blue.com>>white.com>>orange.com>>black.com>>yellow.com
Full CSV output contains geolocation details for ISP and organization, when available. Abbreviated sample output:

domain	ip	mx_hostname	mx_ip	dns	whois_ip_isp	whois_ip_org
orange.com	94.124.133.192	relais-ias241.francetelecom.com.	80.12.204.241	k4.nstld.com.	OAB	france
black.com	72.26.208.183	mail.black.com.	72.26.208.183	ns.voxel.net.	Voxel Dot Net, Inc.	Voxel Dot Net, Inc.
blue.com		mx.dotster.com.	66.96.140.88	ns3.digitalocean.com.			
white.com	193.105.29.73			ns1.yours.pl.	Yours sp. z o.o.	warszawa
red.com	216.146.46.11	red.com.inbound15.mxlogicmx.net.	127.255.255.255	ns3.p09.dynect.net.	Dynamic Network Services, Inc.	Dynamic Network Services, Inc.
yellow.com	54.88.24.144			ns-426.awsdns-53.com.	Amazon Technologies Inc.	Seattle
Monitoring retrieval
The monitoring API service can be used to retrieve a list of active monitors, items within a monitor, and changes detected on items.

All calls to the monitoring service are made to the monitor service. Specify the type of monitor call with the action parameter.

XML and JSON output modes are supported for monitoring calls.

Get a list of active monitors
Use the action=list call to retreive a list of active monitoring reports. This is the equivalent of visiting the http://www.domainiq.com/monitor page.

https://www.domainiq.com/api?key=sample&service=monitor&action=list
Each monitor has a report identifier that can be used in other calls. Sample output:

<`?`xml version="1.0" encoding="UTF-8"?>
<reports>
    <item>
        <report>163</report>
        <type>Keyword</type>
        <name>My Keywords</name>
        <item_count>1</item_count>
        <last_alert_date>2015-02-18 18:10:04</last_alert_date>
    </item>
    <item>
        <report>164</report>
        <type>Registrar</type>
        <name>My Registrars</name>
        <item_count>1</item_count>
        <last_alert_date>2015-02-18 12:00:32</last_alert_date>
    </item>
    <item>
        <report>165</report>
        <type>Email/Registrant</type>
        <name>Set 1</name>
        <item_count>1</item_count>
        <last_alert_date>2015-02-04 18:05:06</last_alert_date>
    </item>
    ...
</reports>
Get a list of items in a monitor
Use the action=report_items call to retrieve a list of items within a monitor. Specify a report parameter that matches the report parameter returned from an entry in the action=list call (e.g. report 163, 164 or 165 in the above output).

https://www.domainiq.com/api?key=sample&service=monitor&action=report_items&report=163
Each monitor item has an item identifier that can be used in other calls. Sample output:

<`?`xml version="1.0" encoding="UTF-8"?>
<items>
    <item>
        <item>51541</item>
        <type>Keyword</type>
        <monitor_name>My Keywords</monitor_name>
        <data>science</data>
        <monitor_start_date>2014-10-29</monitor_start_date>
        <last_alert_date>2015-02-18 18:10:04</last_alert_date>
    </item>
    ...
</items>
Get a daily summary of changes
Next, use the action=report_summary call to get a summary of daily changes detected for a given report item. You can pass a report parameter to get a summary of all changes in a monitor (e.g. 163 above) or an item parameter to get a summary of changes for a specific monitor item (e.g. 51541 above). Optionally, you can specify a range parameter to limit the summary to a certain number of days in the past.

Example: fetch a summary of all changes for all items in report 163.

https://www.domainiq.com/api?key=sample&service=monitor&action=report_summary&report=163
Example: fetch a summary of all changes for item 51541.

https://www.domainiq.com/api?key=sample&service=monitor&action=report_summary&report=163&item=51541
Example: same as the first example, but limit to the last 15 days.

https://www.domainiq.com/api?key=sample&service=monitor&action=report_summary&report=163&range=15
Each daily change has a change identifier. Sample output:

<`?`xml version="1.0" encoding="UTF-8"?>
<changes>
    <item>
        <change>67</change>
        <keyword>science</keyword>
        <domains_added_count>37</domains_added_count>
        <alert_date>2014-10-29</alert_date>
    </item>
    <item>
        <change>71</change>
        <keyword>science</keyword>
        <domains_added_count>39</domains_added_count>
        <alert_date>2014-10-30</alert_date>
    </item>
    ...
</changes>
Get items changed on a particular day
Finally, to get a list of the items changed in a daily change report, make a call with the action=report_changes parameter.

Example: fetch a list of items changed on the 29th (change number 67 in the above example).

https://www.domainiq.com/api?key=sample&service=monitor&action=report_changes&report=163&change=67
Sample output:

<`?`xml version="1.0" encoding="UTF-8"?>
<change_details>
    <item>
        <domain>ameresciences.com</domain>
        <registrar>eNom</registrar>
        <registrant>Whois Agent</registrant>
        <emails>jmpcyqrmgq@whoisprivacyprotect.com</emails>
        <dns>DNS1.NAME-SERVICES.COM</dns>
        <create_date>2014-10-26</create_date>
        <report_date>2014-10-29</report_date>
    </item>
    <item>
        <domain>audioscience.org</domain>
        <registrar>GoDaddy</registrar>
        <registrant>Jason Guppy</registrant>
        <emails>jasonguppy@comcast.net</emails>
        <dns>NS69.DOMAINCONTROL.COM</dns>
        <create_date>2014-10-26</create_date>
        <report_date>2014-10-29</report_date>
    </item>
    ...
</change_details>
Monitoring control
Create a new monitor report
Monitored items are grouped into reports. The API can be used to create a new report to which new items can be added.

Parameters:

type - the type of report to add. Valid types include domain, dns, registrant, organization, email, registrar, keyword, ip, and domain ip. See the Add Monitor page for an explanation of each type.
name - the name to assign to the report.
email_alert - optional - controls whether or not changes in this report will be included in your account's daily monitoring alert email summary. Defaults to on. Pass email_alert=0 to disable.
The number of monitoring reports you can create is dependent on your account. If your account's limit is reached, an error message will be returned; otherwise, upon successful creation of a new report, the report's numeric ID will be returned.

Example: create a new domain report titled "CorporateDomains".

https://www.domainiq.com/api?key=sample&service=monitor&action=report_create&type=domain&name=CorporateDomains
Sample output:

<`?`xml version="1.0" encoding="UTF-8"?>
<result>
    <success>1</success>
    <report_id>112233</report_id>
</result>
Adding items to a report
One or more items can be added to an existing monitoring report using the API.

Parameters:

report_id - the ID of the report to which the item(s) should be added.
type - the type of report to add. Valid types include domain, dns, registrant, organization, email, ip, and domain ip. See the Add Monitor page for an explanation of each type.
item or items - a list of items to add. Item format is dependent on the type selected. Separate multiple items with >>.
The number of items you can monitor of a specific type is dependent on your account's permission levels. If your account's limit is reached for a particular type, an error message will be returned.

Item values and optional attributes for each type:

domain: one or more domain names.
Requires the domain_alert parameter, with one or more of the following to specify which change types should produce alerts, separated by commas:
emails
registrar
dns
create date
expire date
update date
Accepts optional filter parameters.
dns: one or more DNS servers. Subdomains are not required and will be automatically removed if present; DNS servers are monitored by root domain.
registrant: one or more registrant names.
organization: one or more registrant organization names.
email: one or more registrant email addresses.
registrar: one or more registrar names.
Accepts optional filter parameters.
ip: one or more IP addresses.
domain_ip: one or more domain names (for IP monitoring).
keyword: one or more keywords.
Requires the following parameters:
match_types: a set of match types for each keyword supplied in item or items. Separate match types with >>. Possible match types include contains, starts and ends.
join_types: for more than one keyword, a set of join types. Must supply one join type for each keyword. Possible join types are AND and OR.
Optional parameters:
use_typos: boolean; if enabled, also monitors for a set of typo variations for the keyword.
typo_strength: integer from 4 to 41; controls the variance and quantity of typo monitoring. Required when use_typos is specified.
include_added: boolean; defaults to true. Controls whether or not new domain registrations are monitored.
include_deleted: boolean; defaults to true. Controls whether or not domain drops are monitored.
Accepts optional filter parameters.
Filter parameters, available for some monitoring types, allow you to fine-tune the results you'd like to target or exclude. Filter options include:

filter_tld_in, filter_tld_out: accept or reject results by TLD
filter_keyword_in, filter_keyword_in: accept or reject by keyword (anywhere in domain name)
filter_registrar_in, filter_registrar_out: accept or reject by registrar name
filter_dns_in, filter_dns_out: accept or reject by DNS server
filter_email_in, filter_email_out: accept or reject by email address
filter_registrant_in, filter_registrant_out: accept or reject by email address
exclude_dash: disallow domains containing dashes (-)
exclude_num: disallow domains containing numbers (0 through 9)
exclude_idn: disallow IDN domains (domains starting with xn--)
Note that all filters are inclusive; all results must match all filter criteria to be included. For example:

filter_tld_in=net&filter_keyword_in=science&filter_keyword_out=top
That set of filters will only match domains with the .net TLD, containing the keyword science, and not containing the keyword top.

Note that the report to which you're adding items must match the type of the item you're adding. Exceptions are email and registrant items, which can be combined in the same report.

Example: monitor a domain name.

https://www.domainiq.com/api?key=sample&service=monitor&action=report_item_add&report_id=112233&type=domain&item=domainiq.com&domain_alert=emails,registrar,dns
Sample output:

<`?`xml version="1.0" encoding="UTF-8"?>
<success>1</success>
<affected_items>1</affected_items>
Enabling or disabling typos for a keyword monitor
It's possible to programatically control a keyword monitor report item's typo detection using this API call. Reference the keyword monitor by its item ID, available via the report_items call in the item attribute.

Parameters:

report_id: the ID of the monitoring report in which the item is contained.
item_id: the ID of the monitoring item.
strength: optionally specify the typo detection strength. This value can range from 5 to 41. The typo detection algorithm generally controls the number of typos generated; a value of 20 will generate between 17 and 22 typo variants. Defaults to 41 (100%) if not specified.
Example: enable typo monitoring for a keyword report item with 50% (21) strength.

https://www.domainiq.com/api?key=sample&service=monitor&action=enable_typos&report_id=112233&item_id=223344&strength=21
Example: disable typo monitoring for a keyword report item.

https://www.domainiq.com/api?key=sample&service=monitor&action=disable_typos&report_id=112233&item_id=223344
Change typo monitoring strength for a keyword monitor
Additionally, it is possible to modify the strength of a keyword monitor with typo detection enabled. Reference the keyword monitor by its item ID, available via the report_items call in the item attribute.

Parameters:

report_id: the ID of the monitoring report in which the item is contained.
item_id: the ID of the monitoring item.
strength: optionally specify the typo detection strength. This value can range from 5 to 41. The typo detection algorithm generally controls the number of typos generated; a value of 20 will generate between 17 and 22 typo variants. Defaults to 41 (100%) if not specified.
Example: change the typo monitoring strength of a keyword report item to 41 (100%).

https://www.domainiq.com/api?key=sample&service=monitor&action=modify_typo_strength&report_id=112233&item_id=223344&strength=21
Removing items from a report
Use this call to remove an individual monitoring item from a monitoring report. Reference the item by its item ID, available via the report_items call in the item attribute.

Note: this action is permanent and cannot be undone. All history associated with the monitoring item will be removed.

Parameters:

item_id: the ID of the monitoring item to remove.
Example: remove a monitoring item with ID 223344.

https://www.domainiq.com/api?key=sample&service=monitor&action=report_item_delete&item_id=223344
Sample output:

<`?`xml version="1.0" encoding="UTF-8"?>
<success>1</success>
<affected_items>1</affected_items>
Removing an entire report
Use this call to remove an entire monitoring report, including all monitoring items it may contain. Reference the report by its report ID, available via the list call in the report attribute.

Note: this action is permanent and cannot be undone. All history associated with all monitoring items within the report will be removed.

Parameters:

report_id: the ID of the monitoring report to remove.
Example: remove a monitoring report with ID 332211.

https://www.domainiq.com/api?key=sample&service=monitor&action=report_delete&report_id=332211
Sample output:

<`?`xml version="1.0" encoding="UTF-8"?>
<success>1</success>
<affected_items>10</affected_items>
Domain services
Retrieving simple WHOIS records
If you only require WHOIS records for a domain name or IP address, the whois service can be used to perform a live lookup and return the parsed and raw contents of a domain or IP's WHOIS record.

This service uses your daily WHOIS lookup limit. Check your limits here.

Available parameters:

domain - the domain name for WHOIS lookup.
ip - IP address for WHOIS lookup.
full - whether to retrieve a full or abbreviated record.
current_only - if equal to 1, only use current WHOIS record. Only applies to full mode.
The full parameter can be used to control the type of result received from the API. Omitting the parameter will result in a simple parsed version of the WHOIS record as it currently appears for the domain, while passing the full=1 parameter will return a more thoroughly-parsed version of the result that will match what appears on a domain report. Unless current_only=1 is specified, full mode will also attempt to present complete details for parsed fields using past registration records in situations where the domain's registrar is not returning complete fields.

Example: fetch the parsed WHOIS data for google.com.

https://www.domainiq.com/api?key=sample&service=whois&domain=google.com
Sample output:

<`?`xml version="1.0" encoding="UTF-8"?>
<result>
    <domain>google.com</domain>
    <status>ServerUpdateProhibited,ServerTransferProhibited,ClientDeleteProhibited,ServerDeleteProhibited,clientTransferProhibited,ClientUpdateProhibited</status>
    <registrar>MARKMONITOR INC.</registrar>
    <registrar_url>http://www.markmonitor.com</registrar_url>
    <ns_1>NS1.GOOGLE.COM</ns_1>
    <ns_2>NS2.GOOGLE.COM</ns_2>
    <creation_date>1997-09-15</creation_date>
    <expiration_date>2020-09-14</expiration_date>
    <update_date>2011-07-20</update_date>
    <tasting_registrar>No</tasting_registrar>
    <emails>abusecomplaints@markmonitor.com,contact-admin@google.com,dns-admin@google.com</emails>
    <phones>1.2083895740,1.6502530000,1.6506188571,1.6506234000,1.6503300100,1.6506181499,1.8007459229</phones>
    <registrant>Dns Admin</registrant>
    <raw>
        Domain Name: GOOGLE.COM
        Registrar: MARKMONITOR INC.
        Sponsoring Registrar IANA ID: 292
        ...
    </raw>
</result>
DNS record lookup
Look up live DNS records for the specified domain or hostname. The query will return all detected records by default; individual record type(s) can be specified with the type or types parameters. XML and JSON output is supported.

Parameters:

q - hostname or domain for which DNS records will be queried.
type or types - optional; specify which type (singular) or types (plural, comma-delimited) of DNS records to detect. The default is to detect all record types. See below for a list of supported types.
Supported DNS record types include A, A6, AAAA, CNAME, HINFO, MX, NAPTR, NS, PTR, SOA, SRV, TXT.

Example: find all DNS records associated with www.science.com.

https://www.domainiq.com/api?key=sample&service=dns&q=www.science.com
Sample output:

<`?`xml version="1.0" encoding="UTF-8"?>
<results>
    <item>
        <host>redirectserver.aaas.org</host>
        <class>IN</class>
        <ttl>884</ttl>
        <type>A</type>
        <ip>198.151.217.186</ip>
    </item>
    <item>
        <host>www.science.com</host>
        <class>IN</class>
        <ttl>884</ttl>
        <type>CNAME</type>
        <target>redirectserver.aaas.org</target>
    </item>
</results>
Domain categorization
Analyze one or more domains and attempt to categorize them.

This service uses your daily WHOIS lookup limit. Check your limits here.

Parameters:

domain or domains - one or more domain names. Separate multiple domains with >>.
Example: fetch the category for freshdonuts.com.

https://www.domainiq.com/api?key=sample&service=categorize&domain=freshdonuts.com
Sample output:

<`?`xml version="1.0" encoding="UTF-8"?>
<result>
    <item>
        <domain>freshdonuts.com</domain>
        <category>Food -- Baked Goods</category>
    </item>
</result>
Retrieving domain snapshots
You can use the snapshot service to fetch a current or recently-cached snapshot of a domain name's contents.

Parameters:

domain - the domain name to snapshot.
full=1 - if specified, return a full image of the page. The default behavior is to return a 250x125 thumbnail.
no_cache=1 - if specified, do not use a recently-cached image and always fetch a new snapshot.
raw=1 - if specified, return a raw PNG or JPEG image directly instead of a URL to the generated image.
width and height - when generating a thumbnail (the default behavior), specify the width and height of the thumbnail in pixels. Default is 250x125. Minimum 100, maximum 640.
Example: fetch a 250x125 thumbnail of the specified domain.

https://www.domainiq.com/api?key=sample&service=snapshot&domain=msn.com
Sample output:

<`?`xml version="1.0" encoding="UTF-8"?>
<snapshot>http://www.domainiq.com/cache/snapshots/apisnap.msn.com.14f1.png</snapshot>
Example: fetch a full-size image of the specified domain. Return the image directly.

https://www.domainiq.com/api?key=sample&service=snapshot&domain=msn.com&full=1&raw=1
Retrieving historical snapshots
The snapshot_history service can be used to retrieve a list of archived snapshots for the specified domain name. The service will return a list of available snapshots with URLs to retrieve thumbnails and full-size images.

Parameters:

domain - the domain name to look up.
width and height - specify the width and height of the thumbnail images. Default is 250x125. Minimum 100, maximum 640.
limit - limit the results to a specified number. Default is 10; maximum is 50.
Example: fetch the last 10 available snapshots for the specified domain.

https://www.domainiq.com/api?key=sample&service=snapshot_history&domain=msn.com
Sample output:

<`?`xml version="1.0" encoding="UTF-8"?>
<snaps>
    <item>
        <domain>msn.com</domain>
        <format>jpeg</format>
        <date>2012-08-22 16:42:57</date>
        <img_url_thumb>http://snapshots.domainiq.com/img.php?domain=MSN.COM&amp;width=250&amp;height=125&amp;img=...</img_url_thumb>
        <img_url_full>http://snapshots.domainiq.com/img.php?domain=MSN.COM&amp;full=1&amp;img=...</img_url_full>
    </item>
    <item>
        <domain>msn.com</domain>
        <format>png</format>
        <date>2013-04-26 00:32:41</date>
        <img_url_thumb>http://snapshots.domainiq.com/img.php?domain=MSN.COM&amp;width=250&amp;height=125&amp;img=...</img_url_thumb>
        <img_url_full>http://snapshots.domainiq.com/img.php?domain=MSN.COM&amp;full=1&amp;img=...</img_url_full>
    </item>
    ...
</snaps>