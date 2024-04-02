/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.system;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// 기존코드
//@Controller
//class WelcomeController {
//
//	@GetMapping("/")
//	public String welcome() {
//		return "welcome";
//	}

//}


// 새로운코드
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.springframework.ui.Model;

public class NetworkUtils {
    public static String getServerIpAddress() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            return ip.getHostAddress();
        } catch (UnknownHostException e) {
            return "IP 주소를 가져올 수 없습니다.";
        }
    }
}

@Controller
public class WelcomeController {

    @GetMapping("/")
    public String welcome(Model model) {
        String serverIp = NetworkUtils.getServerIpAddress();
        model.addAttribute("serverIp", serverIp);
        return "welcome"; // 여기서 "welcome"은 메인 페이지의 뷰 이름입니다.
    }
}
