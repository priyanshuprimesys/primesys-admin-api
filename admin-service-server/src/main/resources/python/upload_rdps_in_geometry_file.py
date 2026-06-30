import argparse
import os
import pandas as pd
import numpy as np
from pymongo import MongoClient
import re

# MongoDB connection setup
sun_client = MongoClient(
    'mongodb://devuser:ra8Ug0MVqPh7z6lB6i4IMRS5f@139.59.67.169:19213/primesystrack?authSource=admin')
primesystrackDb = sun_client['primesystrack']
rdps_data = primesystrackDb['rdps_geometry']


# Example validate_csv function
import re

def validate_csv(df):
    # Check for required columns
    required_columns = ["Km", "Dist", "Latitude", "Longitude", "Feature Code", "Feature Detail", "Section Name"]
    for col in required_columns:
        if col not in df.columns:
            return f"Missing required column: {col}"

    # Check for entirely empty rows
    empty_rows = (df[df.isnull().all(axis=1)].index + 2).tolist()  # Adding 2 for 1-based index and header row
    if empty_rows:
        return f"The file contains rows with no data (empty rows): {empty_rows}"

    # Check for missing or empty values in required columns except "Feature Code"
    required_columns_except_feature_code = [col for col in required_columns if col != "Feature Code"]
    problematic_rows = {}
    for col in required_columns_except_feature_code:
        missing_or_empty = (df[df[col].isnull() | (df[col].astype(str).str.strip() == '')].index + 2).tolist()
        if missing_or_empty:
            problematic_rows[col] = missing_or_empty

    if problematic_rows:
        return f"Columns with missing or empty values and their rows: {problematic_rows}"

    # Check for non-negative values in 'Km' and 'Dist'
    negative_km_rows = (df[df['Km'] < 0].index + 2).tolist()
    negative_dist_rows = (df[df['Dist'] < 0].index + 2).tolist()
    if negative_km_rows:
        return f"Column 'Km' contains negative values at rows: {negative_km_rows}."
    if negative_dist_rows:
        return f"Column 'Dist' contains negative values at rows: {negative_dist_rows}."

    # Latitude and Longitude format validation
    lat_lon_pattern = re.compile(r"^\d+°\d+'[\d.]+\"[N|S|E|W]$")
    invalid_latitude_rows = (df[~df['Latitude'].fillna('').astype(str).str.strip().apply(lambda x: bool(lat_lon_pattern.match(x)))].index + 2).tolist()
    invalid_longitude_rows = (df[~df['Longitude'].fillna('').astype(str).str.strip().apply(lambda x: bool(lat_lon_pattern.match(x)))].index + 2).tolist()


    if invalid_latitude_rows:
        return f"Column 'Latitude' contains invalid values at rows: {invalid_latitude_rows}."
    if invalid_longitude_rows:
        return f"Column 'Longitude' contains invalid values at rows: {invalid_longitude_rows}."

    return "CSV data is valid."



# Function to create GeoJSON point object
def create_geo_location(row):
    return {
        "type": "Point",
        "coordinates": [row['longitude'], row['latitude']]
    }


# Add feature image column based on conditions
def add_feature_image_column(df):
    imageFolderPath = "~/Images/FeatureCodePhoto/"
    conditions = [
        (df['feature_code'] == 1),
        (df['feature_code'] == 2),
        (df['feature_code'] == 3),
        (df['feature_code'] == 9),
        (df['feature_code'].isin([10, 11])),
        (df['feature_code'] == 21),
        (df['feature_code'].isin([22, 23])),
        (df['feature_code'] == 24),
        (df['feature_code'] == 26),
        (df['feature_code'] == 27)
    ]

    choices = [
        imageFolderPath + 'fc_150.png',
        imageFolderPath + 'fc_2.png',
        imageFolderPath + 'fc_11.png',
        imageFolderPath + 'fc_42.png',
        imageFolderPath + 'fc_102.png',
        imageFolderPath + 'fc_12.png',
        imageFolderPath + 'fc_107.png',
        imageFolderPath + 'fc_24.png',
        imageFolderPath + 'fc__44.png',
        imageFolderPath + 'fc_109.png'
    ]

    df['feature_image'] = np.select(conditions, choices, default=imageFolderPath + "ic_default_featurecode.png")

    return df


def dms_to_dd(dms):
    try:
        # Ensure there are no extraneous characters and standardize input
        dms = dms.replace(' ', '')

        # Regular expression to match the DMS parts
        #   pattern = re.compile(r'(\d+)[°](\d+)[\'\u2032](\d+\.\d+|\d+)[\"\u2033]?([NSEW])')
        pattern = re.compile(r'(\d+)[°](\d+)[\'\u2032](\d+\.\d+|\d+)[\"\u2033]?([NSEW])')
        match = pattern.match(dms)

        if not match:
            raise ValueError(f"Input '{dms}' does not have the correct DMS format.")

        # Extract parts
        if (len(match.group(1)) == 0):
            print(match.group(1))
        if (len(match.group(2)) == 0):
            print(match.group(2))
        degrees = int(match.group(1))
        minutes = int(match.group(2))
        seconds = float(match.group(3))
        direction = match.group(4)

        # Calculate decimal degrees
        dd = degrees + minutes / 60 + seconds / 3600

        # Adjust sign based on direction
        if direction in ['S', 'W']:
            dd = -dd

        return dd
    except Exception as e:
        a = 1 + 2
        # print(f"An error occurred in dms '{dms}': {e}")


# Function to process a single file
def execute_for_single_file(file_path, div_id):
    try:
        # Read CSV file into DataFrame
        df = pd.read_csv(file_path, encoding='latin-1')

        # Validate the CSV
        validation_result = validate_csv(df)
        if validation_result != "CSV data is valid.":
            print(f"Validation failed: {validation_result}")
            sun_client.close()
            return

        # Convert Latitude and Longitude columns to Decimal Degrees
        df['Latitude_decimal'] = df['Latitude'].apply(dms_to_dd)
        df['Longitude_decimal'] = df['Longitude'].apply(dms_to_dd)

        # df['Latitude_decimal'] = df['Latitude']
        # df['Longitude_decimal'] = df['Longitude']

        # Add division ID and create necessary DataFrame for MongoDB
        df['division_id'] = div_id
        df_rdps_geometry = pd.DataFrame()
        df_rdps_geometry['kilometer'] = df['Km']
        df_rdps_geometry['distance'] = df['Dist']
        df_rdps_geometry['latitude'] = df['Latitude_decimal']
        df_rdps_geometry['longitude'] = df['Longitude_decimal']
        df_rdps_geometry['feature_detail'] = df['Feature Detail']
        df_rdps_geometry['feature_code'] = pd.to_numeric(df['Feature Code'], errors='coerce').fillna(999).astype(int)
        df_rdps_geometry['section'] = df['Section Name']
        df_rdps_geometry['division_id'] = df['division_id']
        df_rdps_geometry['active_status'] = True

        df_rdps_geometry['geo_location'] = df_rdps_geometry.apply(create_geo_location, axis=1)

        # Add feature image column
        df_rdps_geometry = add_feature_image_column(df_rdps_geometry)

        # Insert data into MongoDB
        result= rdps_data.insert_many(df_rdps_geometry.to_dict('records'))
        # Calculate inserted and not-inserted counts
        inserted_count = len(result.inserted_ids)
        total_records = len(df_rdps_geometry)
        not_inserted_count = total_records - inserted_count

        print(f"RDPS added successfully for file: {os.path.basename(file_path)}")
        print(f"Total records: {total_records}, Inserted: {inserted_count}, Not Inserted: {not_inserted_count}")

    except FileNotFoundError as e:
        print(f"File not found: {e}")
    except IOError as e:
        print(f"An I/O error occurred: {e}")
    except Exception as e:
        print(f"An error occurred while processing file {os.path.basename(file_path)}: {e}")
    finally:
        sun_client.close()


# Argument parser setup
if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Process a single RDPS CSV file.")
    parser.add_argument('file_path', type=str, help="Path to the CSV file.")
    parser.add_argument('div_id', type=str, help="Division ID for the data.")

    args = parser.parse_args()

    # Execute for the provided file and division ID
    execute_for_single_file(args.file_path, args.div_id)
