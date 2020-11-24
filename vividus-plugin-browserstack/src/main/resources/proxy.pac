function FindProxyForURL(url, host) {
  if (shExpMatch(host, "*.browserstack.com"){
      return "DIRECT";
  }
  if (shExpMatch(host, "<USER_URL>") {
      return "PROXY 10.111.7.45:3128";
  }
  return "DIRECT";
}
