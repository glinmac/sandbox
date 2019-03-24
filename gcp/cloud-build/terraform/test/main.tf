provider "google" {
  version = "2.2.0"
}

provider "google-beta" {
  version = "2.2.0"
}

provider "gsuite" {
  version = "0.1.17"
}


resource "google_service_account" "test" {
  account_id = "deadbeef"
  display_name = "TEST TEST"
}
