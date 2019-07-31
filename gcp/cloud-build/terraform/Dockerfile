FROM alpine:3.10

# Mandatory build args
ARG CLOUDSDK_VERSION
ARG CLOUDSDK_SHA256SUM
ARG TERRAFORM_VERSION
ARG TERRAFORM_SHA256SUM
ARG TERRAFORM_GOOGLE_PROVIDER_VERSION
ARG TERRAFORM_GOOGLE_PROVIDER_SHA256SUM
ARG TERRAFORM_GOOGLE_BETA_PROVIDER_VERSION
ARG TERRAFORM_GOOGLE_BETA_PROVIDER_SHA256SUM
ARG TERRAFORM_GSUITE_PROVIDER_VERSION
ARG TERRAFORM_GSUITE_PROVIDER_SHA256SUM
ARG TERRAFORM_EXTERNAL_PROVIDER_VERSION
ARG TERRAFORM_EXTERNAL_PROVIDER_SHA256SUM
ARG TERRAFORM_RANDOM_PROVIDER_VERSION
ARG TERRAFORM_RANDOM_PROVIDER_SHA256SUM
ARG TERRAFORM_NULL_PROVIDER_VERSION
ARG TERRAFORM_NULL_PROVIDER_SHA256SUM

LABEL maintainer="glinmac@gmail.com"
LABEL description="Terraform Cloud Build image with Google, Google Beta and \
G-Suite provider pre-installed."
LABEL cloudsdk=${CLOUDSDK_VERSION}
LABEL terraform=${TERRAFORM_VERSION}
LABEL terraform-provider-google=${TERRAFORM_GOOGLE_PROVIDER_VERSION}
LABEL terraform-provider-gooole-beta=${TERRAFORM_GOOGLE_BETA_PROVIDER_VERSION}
LABEL terraform-provider-gsuite=${TERRAFORM_GSUITE_PROVIDER_VERSION}
LABEL terraform-provider-random=${TERRAFORM_RANDOM_PROVIDER_VERSION}
LABEL terraform-provider-external=${TERRAFORM_EXTERNAL_PROVIDER_VERSION}
LABEL terraform-provider-null=${TERRAFORM_NULL_PROVIDER_VERSION}


# Dependencies
RUN apk --update --no-cache add \
        curl \
        python \
        py-crcmod \
        bash \
        libc6-compat \
        git \
        gnupg

# GCLOUD SDK
ENV CLOUDSDK_PATH /usr/google-cloud-sdk
ENV PATH ${CLOUDSDK_PATH}/bin:$PATH

RUN CLOUDSDK_URL="https://dl.google.com/dl/cloudsdk/channels/rapid/downloads" && \
    CLOUDSDK_TARBALL="google-cloud-sdk-${CLOUDSDK_VERSION}-linux-x86_64.tar.gz" && \
    CLOUDSDK_SHA256SUMS="clouddsdk_SHA256SUMS" && \
    curl -O ${CLOUDSDK_URL}/${CLOUDSDK_TARBALL} && \
    echo "${CLOUDSDK_SHA256SUM}  ${CLOUDSDK_TARBALL}" > ${CLOUDSDK_SHA256SUMS} && \
    sha256sum -cs ${CLOUDSDK_SHA256SUMS} && \
    tar xzf ${CLOUDSDK_TARBALL} -C /usr && \
    rm -f \
        "${CLOUDSDK_TARBALL}" \
        "${CLOUDSDK_SHA256SUMS}" && \
    ln -s /lib /lib64 && \
    gcloud config set core/disable_usage_reporting true && \
    gcloud config set component_manager/disable_update_check true && \
    gcloud config set metrics/environment github_docker_image && \
    gcloud --version

VOLUME ["/root/.config"]


# TERRAFORM
ENV TERRAFORM_DIR /builder/terraform
ENV PATH ${TERRAFORM_DIR}:$PATH

RUN HASHICORP_URL="https://releases.hashicorp.com" && \
    TERRAFORM_URL="${HASHICORP_URL}/terraform/${TERRAFORM_VERSION}" && \
    TERRAFORM_ZIP="terraform_${TERRAFORM_VERSION}_linux_amd64.zip" && \
    TERRAFORM_SHA256SUMS="terraform_SHA256SUMS" && \
    apk --update --no-cache add \
        git \
        curl && \
    curl -O ${TERRAFORM_URL}/${TERRAFORM_ZIP} && \
    echo "${TERRAFORM_SHA256SUM}  ${TERRAFORM_ZIP}" > ${TERRAFORM_SHA256SUMS} && \
    sha256sum -cs ${TERRAFORM_SHA256SUMS} && \
    mkdir -p ${TERRAFORM_DIR} && \
    unzip ${TERRAFORM_ZIP} -d ${TERRAFORM_DIR} && \
    rm -f \
        "${TERRAFORM_ZIP}" \
        "${TERRAFORM_SHA256SUMS}"


# EXTERNAL PROVIDER
RUN HASHICORP_URL="https://releases.hashicorp.com" && \
    TF_EXTERNAL_ZIP="terraform-provider-external_${TERRAFORM_EXTERNAL_PROVIDER_VERSION}_linux_amd64.zip" && \
    TF_EXTERNAL_URL="${HASHICORP_URL}/terraform-provider-external/${TERRAFORM_EXTERNAL_PROVIDER_VERSION}" && \
    TERRAFORM_SHA256SUMS="terraform_SHA256SUMS" && \
    curl -O ${TF_EXTERNAL_URL}/${TF_EXTERNAL_ZIP} && \
    echo "${TERRAFORM_EXTERNAL_PROVIDER_SHA256SUM}  ${TF_EXTERNAL_ZIP}" > ${TERRAFORM_SHA256SUMS} && \
    sha256sum -cs ${TERRAFORM_SHA256SUMS} && \
    unzip ${TF_EXTERNAL_ZIP} -d ${TERRAFORM_DIR} && \
    rm -f \
        "${TF_EXTERNAL_ZIP}" \
        "${TERRAFORM_SHA256SUMS}"

# RANDOM PROVIDER
RUN HASHICORP_URL="https://releases.hashicorp.com" && \
    TF_RANDOM_ZIP="terraform-provider-random_${TERRAFORM_RANDOM_PROVIDER_VERSION}_linux_amd64.zip" && \
    TF_RANDOM_URL="${HASHICORP_URL}/terraform-provider-random/${TERRAFORM_RANDOM_PROVIDER_VERSION}" && \
    TERRAFORM_SHA256SUMS="terraform_SHA256SUMS" && \
    curl -O ${TF_RANDOM_URL}/${TF_RANDOM_ZIP} && \
    echo "${TERRAFORM_RANDOM_PROVIDER_SHA256SUM}  ${TF_RANDOM_ZIP}" > ${TERRAFORM_SHA256SUMS} && \
    sha256sum -cs ${TERRAFORM_SHA256SUMS} && \
    unzip ${TF_RANDOM_ZIP} -d ${TERRAFORM_DIR} && \
    rm -f \
        "${TF_RANDOM_ZIP}" \
        "${TERRAFORM_SHA256SUMS}"

# NULL PROVIDER
RUN HASHICORP_URL="https://releases.hashicorp.com" && \
    TF_NULL_ZIP="terraform-provider-null_${TERRAFORM_NULL_PROVIDER_VERSION}_linux_amd64.zip" && \
    TF_NULL_URL="${HASHICORP_URL}/terraform-provider-null/${TERRAFORM_NULL_PROVIDER_VERSION}" && \
    TERRAFORM_SHA256SUMS="terraform_SHA256SUMS" && \
    curl -O ${TF_NULL_URL}/${TF_NULL_ZIP} && \
    echo "${TERRAFORM_NULL_PROVIDER_SHA256SUM}  ${TF_NULL_ZIP}" > ${TERRAFORM_SHA256SUMS} && \
    sha256sum -cs ${TERRAFORM_SHA256SUMS} && \
    unzip ${TF_NULL_ZIP} -d ${TERRAFORM_DIR} && \
    rm -f \
        "${TF_NULL_ZIP}" \
        "${TERRAFORM_SHA256SUMS}"

# GOOGLE PROVIDER
RUN HASHICORP_URL="https://releases.hashicorp.com" && \
    TF_GOOGLE_ZIP="terraform-provider-google_${TERRAFORM_GOOGLE_PROVIDER_VERSION}_linux_amd64.zip" && \
    TF_GOOGLE_URL="${HASHICORP_URL}/terraform-provider-google/${TERRAFORM_GOOGLE_PROVIDER_VERSION}" && \
    TERRAFORM_SHA256SUMS="terraform_SHA256SUMS" && \
    curl -O ${TF_GOOGLE_URL}/${TF_GOOGLE_ZIP} && \
    echo "${TERRAFORM_GOOGLE_PROVIDER_SHA256SUM}  ${TF_GOOGLE_ZIP}" > ${TERRAFORM_SHA256SUMS} && \
    sha256sum -cs ${TERRAFORM_SHA256SUMS} && \
    unzip ${TF_GOOGLE_ZIP} -d ${TERRAFORM_DIR} && \
    rm -f \
        "${TF_GOOGLE_ZIP}" \
        "${TERRAFORM_SHA256SUMS}"

# GOOGLE BETA PROVIDER
RUN HASHICORP_URL="https://releases.hashicorp.com" && \
    TF_GOOGLE_BETA_ZIP="terraform-provider-google-beta_${TERRAFORM_GOOGLE_BETA_PROVIDER_VERSION}_linux_amd64.zip" && \
    TF_GOOGLE_BETA_URL="${HASHICORP_URL}/terraform-provider-google-beta/${TERRAFORM_GOOGLE_BETA_PROVIDER_VERSION}" && \
    TERRAFORM_SHA256SUMS="terraform_SHA256SUMS" && \
    curl -O ${TF_GOOGLE_BETA_URL}/${TF_GOOGLE_BETA_ZIP} && \
    echo "${TERRAFORM_GOOGLE_BETA_PROVIDER_SHA256SUM}  ${TF_GOOGLE_BETA_ZIP}" > ${TERRAFORM_SHA256SUMS} && \
    sha256sum -cs ${TERRAFORM_SHA256SUMS} && \
    unzip ${TF_GOOGLE_BETA_ZIP} -d ${TERRAFORM_DIR} && \
    rm -f \
        "${TF_GOOGLE_BETA_ZIP}" \
        "${TERRAFORM_SHA256SUMS}"

# G-SUITE PROVIDER
RUN TF_GSUITE_BASE_URL="https://github.com/DeviaVir/terraform-provider-gsuite/releases/download" && \
    TF_GSUITE_ZIP="terraform-provider-gsuite_${TERRAFORM_GSUITE_PROVIDER_VERSION}_linux_amd64.zip" && \
    TERRAFORM_SHA256SUMS="terraform_SHA256SUMS" && \
    curl -L -O ${TF_GSUITE_BASE_URL}/v${TERRAFORM_GSUITE_PROVIDER_VERSION}/${TF_GSUITE_ZIP} && \
    echo "${TERRAFORM_GSUITE_PROVIDER_SHA256SUM}  ${TF_GSUITE_ZIP}" > ${TERRAFORM_SHA256SUMS} && \
    sha256sum -cs ${TERRAFORM_SHA256SUMS} && \
    unzip ${TF_GSUITE_ZIP} -d ${TERRAFORM_DIR} && \
    rm -f \
        "${TF_GSUITE_ZIP}" \
        "${TERRAFORM_SHA256SUMS}"

ENV TF_IN_AUTOMATION true

ENTRYPOINT ["/builder/terraform/terraform"]
