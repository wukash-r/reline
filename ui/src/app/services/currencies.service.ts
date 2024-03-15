import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Observable} from "rxjs";
import {Currencies} from "../models/currencies";

@Injectable({
  providedIn: 'root'
})
export class CurrenciesService {

  constructor(private httpClient: HttpClient) {  }

  getAvailableCurrencies(): Observable<Currencies> {
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type':  'application/json',
        'Authorization': 'Basic ' + btoa('user:user')
      })
    };

    return this.httpClient.get<Currencies>('http://localhost:8080/currencies', httpOptions);
  }

}