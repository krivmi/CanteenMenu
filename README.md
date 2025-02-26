# Overview
Canteen Menu is an Android widget designed to display the weekly menu of the canteen at Østfold University College directly on your home screen as a widget. The app fetches the menu from the official canteen website and provides an easy-to-read overview of today's and tomorrow's meals. It also shows the current week number and allows users to refresh the data with a single tap.

## Technologies Used
Android SDK
Java
Jsoup Library for web scraping
App Widget Provider

## How It Works
- Data Fetching: Scrapes the weekly menu from the Østfold University College canteen website using Jsoup.
- Day Mapping: Identifies today's and tomorrow's menu based on the current day of the week.
- Widget Interaction: Updates the widget content when clicked on the refresh button.
- The app could be easily adjusted to support different websites of different school canteens.

## Screenshots
<img src="https://github.com/user-attachments/assets/38032b28-4e1e-4d89-8aab-343b5747bb93" alt="Screenshot" width="300">

## Installation
- Clone the repository: git clone https://github.com/krivmi/CanteenMenu.git
- Open the project in Android Studio.
- Sync Gradle and build the project.
- Deploy the app to your Android device.

## License
This project is licensed under the GNU General Public License. See the LICENSE file for details.
