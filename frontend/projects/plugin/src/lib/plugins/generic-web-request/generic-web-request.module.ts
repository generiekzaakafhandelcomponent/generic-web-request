/*
 * Copyright 2026 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {PluginTranslatePipeModule} from "@valtimo/plugin";
import {
  CarbonMultiInputModule,
  FormModule,
  InputModule as ValtimoInputModule,
  SelectModule,
} from "@valtimo/components";
import {GenericWebRequestConfigurationComponent} from "./components/generic-web-request-configuration/generic-web-request-configuration.component";
import {SendRequestConfigurationComponent} from "./components/send-request-configuration/send-request-configuration.component";

@NgModule({
  declarations: [GenericWebRequestConfigurationComponent, SendRequestConfigurationComponent],
  imports: [
    CommonModule,
    PluginTranslatePipeModule,
    FormModule,
    ValtimoInputModule,
    SelectModule,
    CarbonMultiInputModule,
  ],
  exports: [GenericWebRequestConfigurationComponent, SendRequestConfigurationComponent],
})
export class GenericWebRequestPluginModule {}
