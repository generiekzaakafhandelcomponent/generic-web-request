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

import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from "@angular/core";
import {
  PluginConfigurationComponent,
  PluginConfigurationData,
  PluginManagementService,
  PluginTranslationService,
} from "@valtimo/plugin";
import {SelectItem} from "@valtimo/components";
import {TranslateService} from "@ngx-translate/core";
import {BehaviorSubject, combineLatest, map, Observable, Subscription, switchMap, take} from "rxjs";
import {GenericWebRequestConfig} from "../../models";

/** The plugin category of the http-client-authentication plugin, see HttpClientAuthenticator in the backend. */
const AUTHENTICATION_CATEGORY = "http-client-authentication";

@Component({
  standalone: false,
  selector: "valtimo-generic-web-request-configuration",
  templateUrl: "./generic-web-request-configuration.component.html",
})
export class GenericWebRequestConfigurationComponent implements PluginConfigurationComponent, OnInit, OnDestroy {
  @Input() save$!: Observable<void>;
  @Input() disabled$!: Observable<boolean>;
  @Input() pluginId!: string;
  @Input() prefillConfiguration$!: Observable<GenericWebRequestConfig>;
  @Output() valid: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Output() configuration: EventEmitter<PluginConfigurationData> = new EventEmitter<PluginConfigurationData>();

  readonly authenticationPluginSelectItems$: Observable<SelectItem[]>;

  private saveSubscription!: Subscription;
  private readonly formValue$ = new BehaviorSubject<GenericWebRequestConfig | null>(null);
  private readonly valid$ = new BehaviorSubject<boolean>(false);

  constructor(
    private readonly pluginManagementService: PluginManagementService,
    private readonly translateService: TranslateService,
    private readonly pluginTranslationService: PluginTranslationService,
  ) {
    this.authenticationPluginSelectItems$ = combineLatest([
      this.pluginManagementService.getPluginConfigurationsByCategory(AUTHENTICATION_CATEGORY),
      this.translateService.stream("key"),
    ]).pipe(
      map(([configurations]) =>
        configurations.map(configuration => ({
          id: configuration.id ?? "",
          text: `${configuration.title} - ${this.pluginTranslationService.instant(
            "title",
            configuration.pluginDefinition?.key ?? "",
          )}`,
        })),
      ),
    );
  }

  ngOnInit(): void {
    this.openSaveSubscription();
  }

  ngOnDestroy(): void {
    this.saveSubscription?.unsubscribe();
  }

  formValueChange(formValue: GenericWebRequestConfig): void {
    this.formValue$.next(formValue);
    this.handleValid(formValue);
  }

  private handleValid(formValue: GenericWebRequestConfig): void {
    const valid = !!(
      formValue.configurationTitle &&
      /^https?:\/\/\S+$/i.test(formValue.baseUrl) &&
      formValue.authenticationPluginConfiguration
    );
    this.valid$.next(valid);
    this.valid.emit(valid);
  }

  private openSaveSubscription(): void {
    this.saveSubscription = this.save$
      ?.pipe(switchMap(() => combineLatest([this.formValue$, this.valid$]).pipe(take(1))))
      .subscribe(([formValue, valid]) => {
        if (valid && formValue) {
          this.configuration.emit(formValue);
        }
      });
  }
}
