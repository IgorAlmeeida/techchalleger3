// =====================================================================
// Deploy do sistema "agendamento" no Azure Container Apps
// (Spec 08 — 2 Container Apps separados: ca-agendamento + ca-keycloak,
// 1 PostgreSQL Flexible Server free tier com 2 databases)
//
// Uso:
//   az group create --name rg-techchallenger3 --location brazilsouth
//   az deployment group create \
//     --resource-group rg-techchallenger3 \
//     --template-file main.bicep \
//     --parameters postgresAdminPassword=<senha-forte> \
//                  keycloakAdminPassword=<senha-forte> \
//                  gmailUsername=<email> \
//                  gmailAppPassword=<senha-de-app> \
//                  keycloakClientSecret=<secret-do-client>
// =====================================================================

@description('Região do Azure. Confirme disponibilidade do tier gratuito do Postgres Flexible Server nesta região antes de aplicar.')
param location string = 'brazilsouth'

@description('Prefixo usado para nomear os recursos.')
param namePrefix string = 'techchallenger3'

@description('Nome/owner do repositório GHCR, ex: igoralmeeida/techchallenger3 (minúsculo).')
param ghcrImage string = 'ghcr.io/igoralmeeida/techchallenger3:latest'

@description('true se o pacote no GHCR é público (sem necessidade de credenciais de pull).')
param ghcrIsPublic bool = true

@secure()
param postgresAdminPassword string

@secure()
param keycloakAdminPassword string

@secure()
param gmailUsername string

@secure()
param gmailAppPassword string

@secure()
param keycloakClientSecret string

@description('Se o Keycloak deve ter ingress externo (público). Ver Spec 08, seção 2 — default é interno (false).')
param keycloakIngressExterno bool = false

var postgresServerName = 'psql-${namePrefix}'
var postgresAdminUser = 'pgadmin'
var appDbName = 'agendamento'
var keycloakDbName = 'keycloak'
var logAnalyticsName = 'log-${namePrefix}'
var containerAppsEnvName = 'cae-${namePrefix}'
var keycloakAppName = 'ca-keycloak'
var agendamentoAppName = 'ca-agendamento'

// ---------------------------------------------------------------------
// PostgreSQL Flexible Server (free tier: Burstable B1MS, 750h/mês, 12 meses)
// ---------------------------------------------------------------------
resource postgres 'Microsoft.DBforPostgreSQL/flexibleServers@2023-06-01-preview' = {
  name: postgresServerName
  location: location
  sku: {
    name: 'Standard_B1ms'
    tier: 'Burstable'
  }
  properties: {
    version: '16'
    administratorLogin: postgresAdminUser
    administratorLoginPassword: postgresAdminPassword
    storage: {
      storageSizeGB: 32
    }
    backup: {
      backupRetentionDays: 7
      geoRedundantBackup: 'Disabled'
    }
    highAvailability: {
      mode: 'Disabled'
    }
  }
}

resource postgresFirewallAllowAzure 'Microsoft.DBforPostgreSQL/flexibleServers/firewallRules@2023-06-01-preview' = {
  parent: postgres
  name: 'AllowAllAzureServices'
  properties: {
    startIpAddress: '0.0.0.0'
    endIpAddress: '0.0.0.0'
  }
}

resource dbAgendamento 'Microsoft.DBforPostgreSQL/flexibleServers/databases@2023-06-01-preview' = {
  parent: postgres
  name: appDbName
}

resource dbKeycloak 'Microsoft.DBforPostgreSQL/flexibleServers/databases@2023-06-01-preview' = {
  parent: postgres
  name: keycloakDbName
}

// ---------------------------------------------------------------------
// Log Analytics + Container Apps Environment
// ---------------------------------------------------------------------
resource logAnalytics 'Microsoft.OperationalInsights/workspaces@2023-09-01' = {
  name: logAnalyticsName
  location: location
  properties: {
    sku: {
      name: 'PerGB2018'
    }
    retentionInDays: 30
  }
}

resource containerAppsEnv 'Microsoft.App/managedEnvironments@2024-03-01' = {
  name: containerAppsEnvName
  location: location
  properties: {
    appLogsConfiguration: {
      destination: 'log-analytics'
      logAnalyticsConfiguration: {
        customerId: logAnalytics.properties.customerId
        sharedKey: logAnalytics.listKeys().primarySharedKey
      }
    }
  }
}

// ---------------------------------------------------------------------
// Container App: Keycloak
// ---------------------------------------------------------------------
resource keycloakApp 'Microsoft.App/containerApps@2024-03-01' = {
  name: keycloakAppName
  location: location
  properties: {
    environmentId: containerAppsEnv.id
    configuration: {
      ingress: {
        external: keycloakIngressExterno
        targetPort: 8080
        transport: 'http'
      }
      secrets: [
        {
          name: 'keycloak-admin-password'
          value: keycloakAdminPassword
        }
        {
          name: 'postgres-admin-password'
          value: postgresAdminPassword
        }
      ]
    }
    template: {
      containers: [
        {
          name: 'keycloak'
          image: 'quay.io/keycloak/keycloak:24.0'
          args: [
            'start-dev'
          ]
          resources: {
            cpu: json('0.5')
            memory: '1Gi'
          }
          env: [
            { name: 'KC_DB', value: 'postgres' }
            { name: 'KC_DB_URL', value: 'jdbc:postgresql://${postgresServerName}.postgres.database.azure.com:5432/${keycloakDbName}?sslmode=require' }
            { name: 'KC_DB_USERNAME', value: postgresAdminUser }
            { name: 'KC_DB_PASSWORD', secretRef: 'postgres-admin-password' }
            { name: 'KEYCLOAK_ADMIN', value: 'admin' }
            { name: 'KEYCLOAK_ADMIN_PASSWORD', secretRef: 'keycloak-admin-password' }
          ]
        }
      ]
      scale: {
        minReplicas: 1
        maxReplicas: 1
      }
    }
  }
  dependsOn: [
    dbKeycloak
  ]
}

// ---------------------------------------------------------------------
// Container App: agendamento (público)
// ---------------------------------------------------------------------
resource agendamentoApp 'Microsoft.App/containerApps@2024-03-01' = {
  name: agendamentoAppName
  location: location
  properties: {
    environmentId: containerAppsEnv.id
    configuration: {
      ingress: {
        external: true
        targetPort: 8080
        transport: 'http'
      }
      secrets: union([
        { name: 'postgres-admin-password', value: postgresAdminPassword }
        { name: 'gmail-username', value: gmailUsername }
        { name: 'gmail-app-password', value: gmailAppPassword }
        { name: 'keycloak-client-secret', value: keycloakClientSecret }
      ], ghcrIsPublic ? [] : [
        { name: 'ghcr-pull-token', value: '__SUBSTITUA_PELO_PAT_COM_read:packages__' }
      ])
      registries: ghcrIsPublic ? [] : [
        {
          server: 'ghcr.io'
          username: 'igoralmeeida'
          passwordSecretRef: 'ghcr-pull-token'
        }
      ]
    }
    template: {
      containers: [
        {
          name: 'agendamento'
          image: ghcrImage
          resources: {
            cpu: json('0.5')
            memory: '1Gi'
          }
          env: [
            { name: 'SPRING_DATASOURCE_URL', value: 'jdbc:postgresql://${postgresServerName}.postgres.database.azure.com:5432/${appDbName}?sslmode=require' }
            { name: 'POSTGRES_USER', value: postgresAdminUser }
            { name: 'POSTGRES_PASSWORD', secretRef: 'postgres-admin-password' }
            { name: 'GMAIL_USERNAME', secretRef: 'gmail-username' }
            { name: 'GMAIL_APP_PASSWORD', secretRef: 'gmail-app-password' }
            { name: 'KEYCLOAK_CLIENT_SECRET', secretRef: 'keycloak-client-secret' }
            // Nome curto do Container App = DNS interno dentro do mesmo Environment (sem custo/egress externo)
            { name: 'KEYCLOAK_SERVER_URL', value: 'http://${keycloakAppName}' }
            { name: 'SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI', value: 'http://${keycloakAppName}/realms/agendamento' }
            { name: 'SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI', value: 'http://${keycloakAppName}/realms/agendamento/protocol/openid-connect/certs' }
          ]
        }
      ]
      scale: {
        minReplicas: 1
        maxReplicas: 3
      }
    }
  }
  dependsOn: [
    dbAgendamento
    keycloakApp
  ]
}

output urlAgendamento string = 'https://${agendamentoApp.properties.configuration.ingress.fqdn}'
output urlKeycloakInterno string = 'http://${keycloakAppName}'
output postgresHost string = postgres.properties.fullyQualifiedDomainName
