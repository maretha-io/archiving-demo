for file in *; do
    if [ -f "$file" ]; then
        sed -i 's/ai-content-assistant/archiving/g' "$file"
    fi
done

