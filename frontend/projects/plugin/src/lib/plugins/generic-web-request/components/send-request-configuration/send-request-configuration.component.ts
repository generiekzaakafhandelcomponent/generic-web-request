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
import {FunctionConfigurationComponent, FunctionConfigurationData} from "@valtimo/plugin";
import {SelectItem} from "@valtimo/components";
import {BehaviorSubject, combineLatest, Observable, Subscription, switchMap, take} from "rxjs";
import {KeyValue, RequestMethod, SendRequestConfig} from "../../models";

const METHODS: RequestMethod[] = ["GET", "POST", "PUT", "PATCH", "DELETE"];

@Component({
  standalone: false,
  selector: "valtimo-send-request-configuration",
  templateUrl: "./send-request-configuration.component.html",
})
export class SendRequestConfigurationComponent implements FunctionConfigurationComponent, OnInit, OnDestroy {
  @Input() save$!: Observable<void>;
  @Input() disabled$!: Observable<boolean>;
  @Input() pluginId!: string;
  @Input() prefillConfiguration$!: Observable<SendRequestConfig>;
  @Output() valid: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Output() configuration: EventEmitter<FunctionConfigurationData> = new EventEmitter<FunctionConfigurationData>();

  readonly methodSelectItems: SelectItem[] = METHODS.map(method => ({id: method, text: method}));

  private saveSubscription!: Subscription;
  private readonly formValue$ = new BehaviorSubject<SendRequestConfig | null>(null);
  private readonly valid$ = new BehaviorSubject<boolean>(false);

  ngOnInit(): void {
    this.openSaveSubscription();
  }

  ngOnDestroy(): void {
    this.saveSubscription?.unsubscribe();
  }

  formValueChange(formValue: SendRequestConfig): void {
    this.formValue$.next(formValue);
    this.handleValid(formValue);
  }

  private handleValid(formValue: SendRequestConfig): void {
    const valid = METHODS.includes(formValue.method);
    this.valid$.next(valid);
    this.valid.emit(valid);
  }

  private openSaveSubscription(): void {
    this.saveSubscription = this.save$
      ?.pipe(switchMap(() => combineLatest([this.formValue$, this.valid$]).pipe(take(1))))
      .subscribe(([formValue, valid]) => {
        if (valid && formValue) {
          this.configuration.emit(this.clean(formValue));
        }
      });
  }

  /** Leaves out empty fields and rows without a key, so the process link holds only what is set. */
  private clean(formValue: SendRequestConfig): SendRequestConfig {
    const headers = this.cleanRows(formValue.headers);
    const body = this.cleanRows(formValue.body);
    return {
      method: formValue.method,
      ...(formValue.path?.trim() && {path: formValue.path.trim()}),
      ...(headers.length > 0 && {headers}),
      ...(body.length > 0 && {body}),
      ...(formValue.resultVariable?.trim() && {resultVariable: formValue.resultVariable.trim()}),
    };
  }

  private cleanRows(rows: KeyValue[] | undefined): KeyValue[] {
    return (rows ?? [])
      .filter(row => !!row.key?.trim())
      .map(({key, value}) => ({key: key.trim(), value: (value ?? "").trim()}));
  }
}
