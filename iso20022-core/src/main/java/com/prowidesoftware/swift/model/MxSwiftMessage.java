/*
 * Copyright 2006-2020 Prowide
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.prowidesoftware.swift.model;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.prowidesoftware.deprecation.DeprecationUtils;
import com.prowidesoftware.deprecation.ProwideDeprecated;
import com.prowidesoftware.deprecation.TargetYear;
import com.prowidesoftware.swift.io.parser.MxParser;
import com.prowidesoftware.swift.model.mx.*;
import com.prowidesoftware.swift.model.mx.dic.ApplicationHeader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import javax.persistence.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Objects;
import java.util.Optional;

/**
 * MX (ISO 20022) messages entity for JPA persistence.
 *
 * <p>The class holds the full xml content plus message identification metadata gathered from the application header.
 * 
 * <p>Notice, the scope of Prowide MX model is the message payload (the actual message or body data) which is the fundamental
 * purpose of the transmission. The transmission wrappers (overhead data) are excluded and intentionally ignored if found.
 * 
 * <p>MX messages are uniquely identify by their business process, message functionality, variant and version.<br>
 * Consider the following example: trea.001.001.02
 * <ul>
 * <li>trea refers to 'Treasury'</li>
 * <li>001 refers to 'NDF opening (notification)'</li>
 * <li>001 refers to the variant</li>
 * <li>02 refers to the version message format, in this case version 2 of 'NDF opening' type.</li>
 * </ul>
 * 
 * <p>
 * <em>businessProcess</em>: Alphabetic code in four positions (fixed length) identifying the Business Process
 * <br>
 * <em>functionality</em>: Alphanumeric code in three positions (fixed length) identifying the Message Functionality
 * <br>
 * <em>variant</em>: Numeric code in three positions (fixed length) identifying a particular flavor (variant) of Message Functionality
 * <br>
 * <em>version</em>: Numeric code in two positions (fixed length) identifying the version.
 * 
 * @since 7.0
 */
@Entity(name = "mx")
@DiscriminatorValue("mx")
public class MxSwiftMessage extends AbstractSwiftMessage {
	private static final long serialVersionUID = -4394356007627575831L;
	private static final transient java.util.logging.Logger log = java.util.logging.Logger.getLogger(MxSwiftMessage.class.getName());

	@Enumerated(EnumType.STRING)
	@Column(length = 4, name = "business_process")
	private MxBusinessProcess businessProcess;

	@Column(length = 3)
	private String functionality;

	@Column(length = 3)
	private String variant;

	@Column(length = 2)
	private String version;

	public MxSwiftMessage() {
		super();
	}

	/**
	 * Calls {@link #MxSwiftMessage(String, MessageMetadataStrategy)} with the {@link DefaultMxMetadataStrategy}
	 */
	public MxSwiftMessage(final String xml) {
		this(xml, new DefaultMxMetadataStrategy());
	}

	/**
	 * Creates a new message reading the message the content from a string.
	 *
	 * <p>Performs a fast parsing of the header to identify the message.
	 * 
	 * If the string contains several messages, the whole passed content will be save in the message attribute but
	 * identification and metadata will be parser from the first one found only.
	 *
	 * @param xml the plain ISO 20022 XML content, with or without the optional header
	 * @param metadataStrategy a strategy for metadata extraction
	 * @since 9.1.6
	 */
	public MxSwiftMessage(final String xml, final MessageMetadataStrategy metadataStrategy) {
		super(xml, FileFormat.MX, metadataStrategy);
	}
	
	/**
	 * Creates a new message reading the message the content from a string. 
	 * This is a static version of the constructor {@link #MxSwiftMessage(String)}
	 * 
	 * @since 7.7
	 */
	public static MxSwiftMessage parse(final String xml) {
		return new MxSwiftMessage(xml);
	}
	
	/**
	 * Calls {@link #MxSwiftMessage(InputStream, MessageMetadataStrategy)} with the {@link DefaultMxMetadataStrategy}
	 * @since 7.7
	 */
	public MxSwiftMessage(final InputStream stream) throws IOException {
		this(stream, new DefaultMxMetadataStrategy());
	}

	/**
	 * Creates a new message reading the message the content from an input stream.
	 *
	 * @param stream a stream containing the XML message
	 * @param metadataStrategy a strategy for metadata extraction
	 * @since 9.1.6
	 */
	public MxSwiftMessage(final InputStream stream, final MessageMetadataStrategy metadataStrategy) throws IOException {
		super(stream, FileFormat.MX, metadataStrategy);
	}
	
	/**
	 * Creates a new message reading the message the content from an input stream. 
	 * This is a static version of the constructor {@link #MxSwiftMessage(InputStream)}
	 * 
	 * @since 7.7
	 */
	public static MxSwiftMessage parse(final InputStream stream) throws IOException {
		return new MxSwiftMessage(stream);
	}

	/**
	 * Calls {@link #MxSwiftMessage(File, MessageMetadataStrategy)} with the {@link DefaultMxMetadataStrategy}
	 * @since 7.7
	 */
	public MxSwiftMessage(final File file) throws IOException {
		this(file, new DefaultMxMetadataStrategy());
	}

	/**
	 * Creates a new message reading the message the content from a file.
	 *
	 * @param file an existing file containing the XML
	 * @param metadataStrategy a strategy for metadata extraction
	 * @since 9.1.6
	 */
	public MxSwiftMessage(final File file, final MessageMetadataStrategy metadataStrategy) throws IOException {
		super(file, FileFormat.MX, metadataStrategy);
	}
	
	/**
	 * Creates a new message reading the message the content from a file. 
	 * This is a static version of the constructor {@link #MxSwiftMessage(File)}
	 * 
	 * @since 7.7
	 */
	public static MxSwiftMessage parse(final File file) throws IOException {
		return new MxSwiftMessage(file);
	}
	
	/**
	 * Calls {@link #MxSwiftMessage(AbstractMX, MessageMetadataStrategy)} with the {@link DefaultMxMetadataStrategy}
	 * @param mx a message object
	 */
	public MxSwiftMessage(final AbstractMX mx) {
		this(mx, new DefaultMxMetadataStrategy());
	}

	/**
	 * Creates a new message serializing to xml the parameter message object.
	 *
	 * <p>If the business header is present, the sender and receiver attributes will be set with content from the
	 * header; also the internal raw XML will include both 'AppHdr' and 'Document' under a default root element tag
	 * as returned by {@link AbstractMX#message()}
	 *
	 * <br>If the header is not present, sender and receiver will be left null and the raw internal XML will include
	 * just the 'Document' element.
	 *
	 * @param mx a message object
	 * @param metadataStrategy a strategy for metadata extraction
	 * @since 9.1.6
	 */
	public MxSwiftMessage(final AbstractMX mx, final MessageMetadataStrategy metadataStrategy) {
		// instead of reusing the constructor from XML with mx.message() as parameter
		// we set the message and run the update directly to avoid an unnecessary message type detection
		Validate.notNull(mx, "the message model cannot be null");
		Validate.notNull(metadataStrategy, "the strategy for metadata extraction cannot be null");
		setMessage(mx.message());
		_updateFromMessage(mx.getMxId(), metadataStrategy);
	}
	
	/**
	 * Calls {@link #updateFromMessage(MessageMetadataStrategy)} with {@link DefaultMxMetadataStrategy}
	 * @since 7.7
	 */
	@Override
    protected void updateFromMessage() {
		_updateFromMessage(null, new DefaultMxMetadataStrategy());
	}

	/**
	 * Updates the object attributes with metadata parsed from the message raw content using the provided strategy
	 * implementation for several of the metadata fields. The method is called during message creation or update.
	 * @since 9.1.6
	 */
	@Override
	protected void updateFromMessage(final MessageMetadataStrategy metadataStrategy) {
		Validate.notNull(metadataStrategy, "the strategy for metadata extraction cannot be null");
		_updateFromMessage(null, metadataStrategy);
	}

	private void _updateFromMessage(final MxId id, final MessageMetadataStrategy metadataStrategy) {
		if (message() != null && message().length() > 0) {
			MxId identifier = id != null? id : MxParseUtils.identifyMessage(this.message()).orElse(null);
			extractMetadata(identifier, getAppHdr(), metadataStrategy);
		}
	}

	/**
	 * Updates the the attributes with the raw message and its metadata from the given raw (XML) message content.
	 *
	 * @param mx the new message content
	 * @see #updateFromMessage()
	 * @since 7.8.4
	 */
	public void updateFromModel(final AbstractMX mx) {
		updateFromModel(mx, new DefaultMxMetadataStrategy());
	}

	public void updateFromModel(final AbstractMX mx, final MessageMetadataStrategy metadataStrategy) {
		Validate.notNull(mx, "the mx parameter cannot be null");
		Validate.notNull(metadataStrategy, "the strategy for metadata extraction cannot be null");
		setMessage(mx.message());
		setFileFormat(FileFormat.MX);
		extractMetadata(mx.getMxId(), mx.getAppHdr(), metadataStrategy);
	}

	private void extractMetadata(MxId identifier, AppHdr headerModel, MessageMetadataStrategy metadataStrategy) {
		MxNode parsedMessage = MxNode.parse(this.message());
		if (headerModel == null || !extractMetadata(headerModel)) {
			extractMetadata(parsedMessage);
		}

		if (identifier != null) {
			this.identifier = identifier.id();
			this.businessProcess = identifier.getBusinessProcess();
			this.functionality = identifier.getFunctionality();
			this.variant = identifier.getVariant();
			this.version = identifier.getVersion();
		}

		applyStrategy(getMessage(), metadataStrategy);
	}

	/**
	 * Updates sender, receiver and reference from parameter header
	 * @return true if at least some property was updated
	 */
	private boolean extractMetadata(AppHdr h) {
		boolean updated = false;
		if (h != null) {
			final String from = h.from();
			if (from != null) {
				super.sender = bic11(from);
				updated = true;
			}

			final String to = h.to();
			if (to != null) {
				super.receiver = bic11(to);
				updated = true;
			}
		}
		return updated;
	}

	/**
	 * Updates sender and receiver from the group header element (only present in a subset of Mx messages)
	 * @return true if at least some property was updated
	 */
	private boolean extractMetadata(MxNode n) {
		boolean updated = false;
		final MxNode groupHeader = n != null? n.findFirstByName("GrpHdr") : null;
		if (groupHeader != null) {
			MxNode senderBic = groupHeader.findFirst("./InstgAgt/FinInstnId/BIC");
			if (senderBic != null) {
				sender = bic11(senderBic.getValue());
				updated = true;
			}
			MxNode receiverBic = groupHeader.findFirst("./InstdAgt/FinInstnId/BIC");
			if (receiverBic != null) {
				receiver = bic11(receiverBic.getValue());
				updated = true;
			}
		}
		return updated;
	}

	/**
	 * Calls {@link #updateFromXML(String, MxId, MessageMetadataStrategy)} with {@link DefaultMxMetadataStrategy}
	 * @since 7.8.4
	 */
	public void updateFromXML(final String xml) {
		updateFromXML(xml, null);
	}

	/**
	 * Calls {@link #updateFromXML(String, MxId, MessageMetadataStrategy)} with {@link DefaultMxMetadataStrategy}
	 * @since 7.8.4
	 */
	public void updateFromXML(final String xml, final MxId id) {
		updateFromXML(xml, id, new DefaultMxMetadataStrategy());
	}

	/**
	 * Updates the the attributes with the raw message and its metadata from the given raw (XML) message content.
	 * Wrapper around AppHdr/Document, if present, are preserved and ignored.
	 *
	 * @param xml the XML content of an MX message containing the Document and optional AppHdr elements
	 * @param id the specific Mx type identification or null if message is unknown
	 * @param metadataStrategy the strategy implementation to use for metadata extraction
	 * @since 9.1.6
	 */
	public void updateFromXML(final String xml, final MxId id, final MessageMetadataStrategy metadataStrategy) {
		Validate.notNull(xml, "the xml message parameter cannot be null");
		Validate.notNull(metadataStrategy, "the strategy for metadata extraction cannot be null");
		setMessage(xml);
		setFileFormat(FileFormat.MX);
		_updateFromMessage(id, metadataStrategy);
	}

	public MxBusinessProcess getBusinessProcess() {
		return businessProcess;
	}

	public void setBusinessProcess(MxBusinessProcess businessProcess) {
		this.businessProcess = businessProcess;
	}

	public String getFunctionality() {
		return functionality;
	}

	public void setFunctionality(String functionality) {
		this.functionality = functionality;
	}

	public String getVariant() {
		return variant;
	}

	public void setVariant(String variant) {
		this.variant = variant;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		MxSwiftMessage that = (MxSwiftMessage) o;
		return businessProcess == that.businessProcess &&
				Objects.equals(functionality, that.functionality) &&
				Objects.equals(variant, that.variant) &&
				Objects.equals(version, that.version);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), businessProcess, functionality, variant, version);
	}

	/**
	 * @deprecated use {@link #getAppHdr()} instead
	 */
	@Deprecated
	@ProwideDeprecated(phase2 = TargetYear.SRU2021)
	public BusinessHeader getBusinessHeader() {
		MxParser parser = new MxParser(this.message());
		return parser.parseBusinessHeader();
	}

	/**
	 * If present in the message content, returns the business header (SWIFT or ISO version)
	 * Notice this header is optional and may not be present.
	 * @see AppHdrParser#parse(String)
	 * @return found header or null if not present or cannot be parsed into a header object
	 * @since 9.0.1
	 */
	public AppHdr getAppHdr() {
		return AppHdrParser.parse(this.message()).orElse(null);
	}

	/**
	 * @deprecated use {@link #getAppHdr()} instead
	 */
	@Deprecated
	@ProwideDeprecated(phase4=TargetYear.SRU2021)
	public ApplicationHeader getApplicationHeader() {
		DeprecationUtils.phase3(getClass(), "getApplicationHeader()", "use getAppHdr() instead");
		Optional<AppHdr> optionalAppHdr = AppHdrParser.parse(this.message());
		if (optionalAppHdr.isPresent() && optionalAppHdr.get() instanceof LegacyAppHdr) {
			ApplicationHeader result = new ApplicationHeader();
			LegacyAppHdr legacyHdr = (LegacyAppHdr) optionalAppHdr.get();
			result.setFrom(legacyHdr.getFrom());
			result.setTo(legacyHdr.getTo());
			result.setSvcName(legacyHdr.getSvcName());
			result.setMsgName(legacyHdr.getMsgName());
			result.setMsgRef(legacyHdr.getMsgRef());
			result.setCrDate(legacyHdr.getCrDate());
			result.setDup(legacyHdr.getDup());
			return result;
		}
		return null;
	}

	/**
	 * The application header is no longer stored as class attribute
	 * @see #getApplicationHeader()
	 * @deprecated @see #getApplicationHeader()
	 */
	@Deprecated
	@ProwideDeprecated(phase4=TargetYear.SRU2021)
	public void setApplicationHeader(ApplicationHeader applicationHeader) {
		String message = "Obsolete API call. The application header is no longer stored as class attribute in "+getClass().getName();
		DeprecationUtils.phase3(getClass(), "setApplicationHeader(ApplicationHeader)", message);
	}
	
	/**
	 * Creates a full copy of the current message object into another message.
	 * @param msg target message
	 * @since 7.7
	 * @see AbstractSwiftMessage#copyTo(AbstractSwiftMessage)
	 */
	public void copyTo(MxSwiftMessage msg) {
	    super.copyTo(msg);
	    msg.setBusinessProcess(getBusinessProcess());
	    msg.setFunctionality(getFunctionality());
	    msg.setVariant(getVariant());
	    msg.setVersion(getVersion());
	}

	/**
	 * @since 7.8.6
	 */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("MxSwiftMessage id=").append(getId()).append(" message=").append(getMessage());
		return sb.toString();
	}

	/**
	 * This method deserializes the JSON data into an MX message object.
	 * @see #toJson()
	 * @since 7.10.3
	 */
	public static MxSwiftMessage fromJson(String json){
		final Gson gson = new GsonBuilder().create();
		return gson.fromJson(json, MxSwiftMessage.class);
	}

	/**
	 * Returns this message MX identification
	 * @return the identification object for this message
	 * @since 7.10.4
	 */
	public MxId getMxId() {
		return new MxId(this.businessProcess, this.functionality, this.variant, this.version);
	}

	/**
	 * For MT messages returns the category number and for MX messages return the business process.
	 * For example for MT103 returns 1 and for pacs.004.001.06 returns pacs
	 * @return a string with the category or empty if the identifier is invalid or not present
	 * @since 7.10.4
	 */
	@Override
	public String getCategory() {
		if (!StringUtils.isBlank(this.identifier)) {
			MxBusinessProcess proc = (new MxId(this.identifier)).getBusinessProcess();
			if (proc != null) {
				return proc.name();
			}
		}
		return "";
	}

	/**
	 * Enables injecting your own implementation for the entity metadata extraction, to set the generic properties
	 * shared by all message types: main reference, main amount and currency, value date, trade date.
	 *
	 * @since 9.1.6
	 */
	public void updateMetadata(MessageMetadataStrategy strategy) {
		Validate.notNull(strategy, "the strategy for metadata extraction cannot be null");
		applyStrategy(getMessage(), strategy);
	}

	private void applyStrategy(String xml, MessageMetadataStrategy strategy) {
		boolean isKnownType = this.businessProcess != null && this.functionality != null && this.variant != null && this.version != null;
		AbstractMX mx = isKnownType? AbstractMX.parse(xml, getMxId()) : AbstractMX.parse(xml);

		if (mx == null) {
			// could not parse the XML into a message model
			return;
		}

		String reference = strategy.reference(mx).orElse(null);
		setReference(reference);

		Optional<Money> money = strategy.amount(mx);
		if (money.isPresent()) {
			setCurrency(money.get().getCurrency());
			setAmount(money.get().getAmount());
		}

		Calendar valueDate = strategy.valueDate(mx).orElse(null);
		setValueDate(valueDate);

		Calendar tradeDate = strategy.tradeDate(mx).orElse(null);
		setTradeDate(tradeDate);
	}

}
