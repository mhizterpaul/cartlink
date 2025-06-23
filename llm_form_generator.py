import argparse
import json
import hashlib
import spacy
# from langchain.llms import OpenAI  # Uncomment and configure as needed
# from langchain.prompts import PromptTemplate

# Load spaCy English model
nlp = spacy.load("en_core_web_sm")

def extract_representative_word(product_type):
    doc = nlp(product_type)
    # Try to get the root noun or the most important word
    for token in doc:
        if token.pos_ == "NOUN":
            return token.lemma_.lower()
    # fallback: use root or first word
    return doc[0].lemma_.lower() if doc else product_type.lower()

def generate_type_id(word):
    # Use a hash for uniqueness
    return hashlib.sha256(word.encode()).hexdigest()[:12]

def main():
    parser = argparse.ArgumentParser(description="LLM Product Form Generator")
    parser.add_argument('--category', required=True)
    parser.add_argument('--productType', required=True)
    parser.add_argument('--brand', required=True)
    parser.add_argument('--name', required=True)
    parser.add_argument('--description', required=True)
    args = parser.parse_args()

    rep_word = extract_representative_word(args.productType)
    type_id = generate_type_id(rep_word)

    # Fields used for typeId/form: category, productType, brand, name, description
    # Product fields to enrich with (excluding 'specifications' for HTML form):
    additional_fields = [
        {"name": "productionYear", "type": "Integer", "label": "Production Year"},
        {"name": "price", "type": "Decimal", "label": "Price"},
        {"name": "unitsInStock", "type": "Integer", "label": "Units in Stock"},
        {"name": "payOnDelivery", "type": "Boolean", "label": "Pay on Delivery"},
        {"name": "images", "type": "List<String>", "label": "Product Images"},
        {"name": "videos", "type": "List<String>", "label": "Product Videos"},
        {"name": "imported", "type": "Boolean", "label": "Imported"}
        # 'specifications' intentionally omitted from HTML form fields
    ]

    # Example prompt template (replace with actual LangChain logic)
    prompt = f"""
    Generate relevant product attributes for a productType '{args.productType}' in the '{args.category}' category.\n
    Product Name: {args.name}\nBrand: {args.brand}\nDescription: {args.description}\n
    Respond in JSON with suggested field names, types, and descriptions.
    """

    # Placeholder for LLM call (replace with actual LangChain/OpenAI call)
    # llm = OpenAI()
    # response = llm(prompt)
    # For now, use a static example:
    llm_fields = [
        {"name": "screenSizeInches", "type": "Decimal", "label": "Screen Size (inches)"},
        {"name": "batteryCapacityMah", "type": "Integer", "label": "Battery (mAh)"},
        {"name": "storageGb", "type": "Integer", "label": "Storage (GB)"},
        {"name": "ramGb", "type": "Integer", "label": "RAM (GB)"}
    ]
    all_fields = llm_fields + additional_fields
    response = {
        "typeId": type_id,
        "representativeWord": rep_word,
        "fields": all_fields
    }
    print(json.dumps(response))

if __name__ == "__main__":
    main() 