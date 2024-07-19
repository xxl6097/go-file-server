package main

import (
	"fmt"
	"net"
)

// isPrivateIP 检查IP是否为私有地址
func isPrivateIP(ip net.IP) bool {
	if ip.IsLoopback() || ip.IsLinkLocalUnicast() {
		return true
	}
	ip = ip.To4()
	if ip == nil {
		return false
	}
	return ip[0] == 10 || (ip[0] == 172 && ip[1] >= 16 && ip[1] <= 31) || (ip[0] == 192 && ip[1] == 168)
}

// GetHostIp 获取私有地址
func GetHostIp() string {
	addrList, err := net.InterfaceAddrs()
	if err != nil {
		fmt.Println("get current host ip err: ", err)
		return ""
	}
	//var ips []net.IP
	for _, address := range addrList {
		if ipNet, ok := address.(*net.IPNet); ok && !ipNet.IP.IsLoopback() && ipNet.IP.IsPrivate() {
			if ipNet.IP.To4() != nil {
				//ip = ipNet.IP.String()
				//break
				ip := ipNet.IP.To4()
				//fmt.Println(ip[0])
				switch ip[0] {
				case 10:
					return ipNet.IP.String()
				case 192:
					return ipNet.IP.String()
				}
			}
		}
	}
	//fmt.Println(ips)
	return ""
}
