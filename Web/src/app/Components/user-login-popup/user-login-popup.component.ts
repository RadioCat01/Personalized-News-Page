import { Component, Inject } from '@angular/core';
import { FormArray, FormBuilder, FormGroup } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { CheckUSerService } from '../../Services/User/check-user.service';
import { SharedService } from '../../Services/SharedService/shared.service';
import {flush} from "@angular/core/testing";

@Component({
  selector: 'app-user-login-popup',
  templateUrl: './user-login-popup.component.html',
  styleUrl: './user-login-popup.component.scss'
})
export class UserLoginPopupComponent {
  preferencesForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private checkUserService: CheckUSerService,
    public dialogRef: MatDialogRef<UserLoginPopupComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private sharedService: SharedService
  ) {
    this.preferencesForm = this.fb.group({
      Technology: false,
      Sports: false,
      health: false,
      Global_Politics: false,
      Economy:false,
      Finance:false,
      Gaming:false,
      Automotive:false,
      Science:false,
      Business:false,
      Health:false,
      Cryptocurrency:false,
      History:false,
      Philanthropy:false,
      Parenting:false,
      Music:false,
      Space_Exploration:false,
      Alternative_Medicine:false,
      Artificial_Intelligence:false,
      Home_Improvement:false,
      Books_Literature:false,
      Climate:false,
      Electric_Vehicles:false,
      Economic_Recession:false,
      Geopolitical_Tensions:false,
      TechRegulations:false,
      SocialMedia:false
    });
  }

  onSave(): void {
    const selectedPreferences = this.getSelectedPreferences();
    this.checkUserService.savePreferences(selectedPreferences)
      .subscribe(response => {
        console.log('Preferences saved successfully:', response);
        this.dialogRef.close(true);
        this.sharedService.notifyPreferencesUpdated();
      }, error => {
        console.error('Error saving preferences:', error);
      });
  }

  togglePreference(preference: string): void {
    const formControl = this.preferencesForm.get(preference);
    if (formControl) {

      formControl.setValue(!formControl.value);
      console.log(formControl.value);
    }
  }

  private getSelectedPreferences(): string[] {
    const preferences = this.preferencesForm.value;
    return Object.keys(preferences).filter(key => preferences[key]);
  }
}
