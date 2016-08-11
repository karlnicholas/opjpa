/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package opjpa;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import codesparser.SectionNumber;
import statutews.StatuteKey;
import statutews.StatuteKeyArray;
import statutews.StatuteViewArray;
import statutews.StatuteWS;

import java.lang.System;
import java.net.URL;

/**
 *
 * @author rsearls@redhat.com
 */
public class StatuteWSClient {

    public static void main(String[] args)
    {
        String endPointAddress = "http://localhost:8080/statutews-endpoint/StatuteWS";
        QName serviceName = new QName("http://statutews/", "StatuteWSService");

        try {
            URL wsdlURL = new URL(endPointAddress + "?wsdl");
            Service service = Service.create(wsdlURL, serviceName);
            StatuteWS proxy = service.getPort(StatuteWS.class);

            StatuteKey key = new StatuteKey();
            
            key.setCode("California Penal Code");
            key.setSectionNumber("625");
            StatuteKeyArray statuteKeyArray = new StatuteKeyArray();
            statuteKeyArray.getItem().add(key);
            StatuteViewArray viewArray = proxy.echo(statuteKeyArray);
            System.out.println(viewArray);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
