$templatesDir = "E:\Fourth Semester\Cloud Mind\Cloud-Mind\src\main\resources\templates"
$htmlFiles = Get-ChildItem -Path $templatesDir -Filter "*.html"

foreach ($file in $htmlFiles) {
    $content = Get-Content -Path $file.FullName -Raw
    
    # Update CSS path
    $content = $content -replace '<link rel="stylesheet" href="styles.css">', '<link rel="stylesheet" href="/Images/css/styles.css">'
    
    # Update JS path
    $content = $content -replace '<script src="script.js"></script>', '<script src="/Images/js/script.js"></script>'
    
    # Update image paths
    $content = $content -replace 'src="images/logoF.png"', 'src="/Images/logoF.png"'
    
    # Write the updated content back to the file
    Set-Content -Path $file.FullName -Value $content
}

Write-Host "All HTML files have been updated with correct resource paths."