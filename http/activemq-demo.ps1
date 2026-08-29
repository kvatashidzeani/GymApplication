# ActiveMQ end-to-end demo (no IntelliJ license needed)
# Prerequisites: ActiveMQ :61616, GymRestApplication :8081, WorkloadApplication :8082
# Uses unique demo names (seed data already has John.Doe / Mike.Brown).

$ErrorActionPreference = "Stop"
$hostUrl = "http://localhost:8081"
$workloadHost = "http://localhost:8082"

function Write-Step($msg) { Write-Host "`n=== $msg ===" -ForegroundColor Cyan }

Write-Step "0) Health checks (warnings only — demo continues)"
try {
    $gymHealth = Invoke-RestMethod -Uri "$hostUrl/actuator/health" -Method Get -ErrorAction Stop
    Write-Host "Gym CRM:    $($gymHealth.status)"
} catch {
    Write-Host "Gym CRM:    UNAVAILABLE ($($_.Exception.Message))" -ForegroundColor Yellow
    Write-Host "  -> Start GymRestApplication on :8081 and ActiveMQ on :61616" -ForegroundColor Yellow
}
try {
    $workloadHealth = Invoke-RestMethod -Uri "$workloadHost/actuator/health" -Method Get -ErrorAction Stop
    Write-Host "Workload:   $($workloadHealth.status)"
} catch {
    Write-Host "Workload:   UNAVAILABLE ($($_.Exception.Message))" -ForegroundColor Yellow
    Write-Host "  -> Start WorkloadApplication on :8082" -ForegroundColor Yellow
}

Write-Step "1) Register trainer"
$trainerBody = @{
    firstName = "Amy"
    lastName = "Coach"
    specialization = "Cardio"
} | ConvertTo-Json
$trainer = Invoke-RestMethod -Uri "$hostUrl/trainers/register" -Method Post -Body $trainerBody -ContentType "application/json"
Write-Host "trainer_username=$($trainer.username)"
Write-Host "trainer_password=$($trainer.password)"

Write-Step "2) Register trainee"
$traineeBody = @{
    firstName = "Sam"
    lastName = "Member"
    dateOfBirth = "1998-05-12"
    address = "Tbilisi"
} | ConvertTo-Json
$trainee = Invoke-RestMethod -Uri "$hostUrl/trainees/register" -Method Post -Body $traineeBody -ContentType "application/json"
Write-Host "trainee_username=$($trainee.username)"
Write-Host "trainee_password=$($trainee.password)"

$traineeHeaders = @{ Authorization = "Bearer $($trainee.token)" }
$trainerHeaders = @{ Authorization = "Bearer $($trainer.token)" }

Write-Step "3) Assign trainer to trainee"
$assignBody = @{
    traineeUsername = $trainee.username
    trainersList = @(@{ username = $trainer.username })
} | ConvertTo-Json -Depth 3
Invoke-RestMethod -Uri "$hostUrl/trainees/$($trainee.username)/trainers" -Method Put -Headers $traineeHeaders -Body $assignBody -ContentType "application/json" | Out-Null
Write-Host "OK"

Write-Step "4) Add training (publishes ADD to ActiveMQ)"
$trainingBody = @{
    traineeUsername = $trainee.username
    trainerUsername = $trainer.username
    trainingName = "Morning Cardio"
    trainingDate = "2026-08-29"
    trainingDuration = 60
} | ConvertTo-Json
Invoke-RestMethod -Uri "$hostUrl/trainings" -Method Post -Headers $traineeHeaders -Body $trainingBody -ContentType "application/json" | Out-Null
Write-Host "OK - check Gym CRM log for: Published workload ADD to queue=workload.events.queue"

Write-Host "Waiting 2s for async JMS..." -ForegroundColor Yellow
Start-Sleep -Seconds 2

Write-Step "5) Verify monthly workload (expect trainingSummaryDuration=60)"
$month = Invoke-RestMethod -Uri "$workloadHost/workload/$($trainer.username)/2026/8" -Method Get -Headers $trainerHeaders
$month | ConvertTo-Json -Compress | Write-Host
if ($month.trainingSummaryDuration -eq 60) {
    Write-Host "SUCCESS: workload updated via ActiveMQ" -ForegroundColor Green
} else {
    Write-Host "WARN: expected trainingSummaryDuration=60, got $($month.trainingSummaryDuration)" -ForegroundColor Yellow
}

Write-Step "6) Full trainer workload summary"
$summary = Invoke-RestMethod -Uri "$workloadHost/workload/$($trainer.username)" -Method Get -Headers $trainerHeaders
$summary | ConvertTo-Json -Depth 5 | Write-Host

Write-Step "7) Optional login with saved password"
$loginUri = "$hostUrl/login?username=$([uri]::EscapeDataString($trainee.username))&password=$([uri]::EscapeDataString($trainee.password))"
$login = Invoke-RestMethod -Uri $loginUri -Method Get
Write-Host "trainee re-login OK, token length=$($login.token.Length)"

Write-Host "`nDone." -ForegroundColor Green
